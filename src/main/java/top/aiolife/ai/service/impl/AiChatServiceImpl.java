package top.aiolife.ai.service.impl;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.aiolife.ai.langchain4j.AiChatMemoryFactory;
import top.aiolife.ai.langchain4j.AiPromptContextBuilder;
import top.aiolife.ai.langchain4j.AiServiceFactory;
import top.aiolife.ai.langchain4j.AiServiceRuntime;
import top.aiolife.ai.memory.pojo.vo.AiMemoryVO;
import top.aiolife.ai.memory.service.AiMemoryService;
import top.aiolife.ai.pojo.req.AiChatReq;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.ai.pojo.vo.AiChatResp;
import top.aiolife.ai.service.AiAgentConfigService;
import top.aiolife.ai.service.AiChatService;
import top.aiolife.ai.tool.AiToolService;
import top.aiolife.llm.pojo.entity.LLMKeyEntity;
import top.aiolife.llm.service.ChatMessageService;
import top.aiolife.llm.service.LLMKeyService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认 AI 聊天编排服务实现。
 *
 * @author Ethan
 * @date 2026-07-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final String DEFAULT_AGENT_CODE = "life_assistant";

    private static final long SSE_TIMEOUT_MILLIS = 300_000L;

    private final LLMKeyService llmKeyService;

    private final ChatMessageService chatMessageService;

    private final AiAgentConfigService aiAgentConfigService;

    private final AiServiceFactory aiServiceFactory;

    private final AiChatMemoryFactory aiChatMemoryFactory;

    private final AiPromptContextBuilder aiPromptContextBuilder;

    private final AiMemoryService aiMemoryService;

    private final AiToolService aiToolService;

    /**
     * 发送非流式 AI 聊天请求，并保存用户原始消息和助手回复。
     *
     * @param userId 当前登录用户 id
     * @param req AI 聊天请求，包含 Agent 编码、会话 id、用户消息和兼容旧逻辑的上下文
     * @return AI 聊天响应，包含 Agent 编码、会话 id、回复内容和模型名称
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Override
    public AiChatResp chat(Long userId, AiChatReq req) {
        AiChatReq safeReq = normalizeRequest(req);
        AiAgentConfigVO agentConfig = getEnabledAgentConfig(userId, safeReq.getAgentCode());
        LLMKeyEntity llmKey = resolveLlmKey(userId, agentConfig.getModelKeyId());
        List<AiMemoryVO> memories = aiMemoryService.listEffectiveMemories(userId, safeReq.getAgentCode(), agentConfig.getMaxMemoryItems());
        String systemMessage = aiPromptContextBuilder.buildSystemMessage(agentConfig.getSystemPrompt(), memories, safeReq.getContext());
        ChatMemory chatMemory = aiChatMemoryFactory.createMemory(userId, safeReq.getAgentCode(), safeReq.getConversationId(), agentConfig.getMaxContextMessages());
        Map<ToolSpecification, ToolExecutor> tools = aiToolService.buildTools(userId, agentConfig);
        Double temperature = toDouble(agentConfig.getTemperature());
        AiServiceRuntime runtime = aiServiceFactory.createRuntime(agentConfig, llmKey, systemMessage, chatMemory, tools, temperature);

        chatMessageService.saveMessage(userId, safeReq.getConversationId(), "user", safeReq.getMessage(), llmKey.getModelName());
        String response = runtime.getAssistantService().chat(safeReq.getMessage());
        chatMessageService.saveMessage(userId, safeReq.getConversationId(), "assistant", response, llmKey.getModelName());

        AiChatResp resp = new AiChatResp();
        resp.setAgentCode(runtime.getAgentCode());
        resp.setConversationId(safeReq.getConversationId());
        resp.setContent(response);
        resp.setModelName(runtime.getModelName());
        resp.setAgentName(runtime.getAgentName());
        resp.setSystemPromptApplied(runtime.getSystemPromptApplied());
        return resp;
    }

    /**
     * 发送流式 AI 聊天请求，并保存用户原始消息和助手回复。
     *
     * @param userId 当前登录用户 id
     * @param req AI 聊天请求，包含 Agent 编码、会话 id、用户消息和兼容旧逻辑的上下文
     * @return SSE 发送器，使用 token、done 和 error 事件输出结构化 JSON 数据
     *
     * @author Ethan
     * @date 2026-07-19
     */
    @Override
    public SseEmitter chatStream(Long userId, AiChatReq req) {
        return chatStream(userId, req, false);
    }

    /**
     * 发送兼容旧客户端协议的流式 AI 聊天请求。
     *
     * @param userId 当前登录用户 id
     * @param req AI 聊天请求，包含 Agent 编码、会话 id、用户消息和兼容旧逻辑的上下文
     * @return SSE 发送器，使用原始 token、[DONE] 和 [ERROR] 标记输出数据
     *
     * @author Ethan
     * @date 2026-07-19
     */
    @Override
    public SseEmitter chatStreamLegacy(Long userId, AiChatReq req) {
        return chatStream(userId, req, true);
    }

    private SseEmitter chatStream(Long userId, AiChatReq req, boolean legacyProtocol) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        StringBuffer fullResponse = new StringBuffer();
        AtomicBoolean terminated = new AtomicBoolean(false);

        emitter.onTimeout(() -> terminateEmitter(emitter, terminated, "AI streaming response timed out"));
        emitter.onError(error -> terminateEmitter(emitter, terminated, "AI streaming client disconnected"));
        emitter.onCompletion(() -> {
            if (terminated.compareAndSet(false, true)) {
                log.info("AI streaming client connection completed before model response");
            }
        });

        try {
            AiChatReq safeReq = normalizeRequest(req);
            AiAgentConfigVO agentConfig = getEnabledAgentConfig(userId, safeReq.getAgentCode());
            LLMKeyEntity llmKey = resolveLlmKey(userId, agentConfig.getModelKeyId());
            List<AiMemoryVO> memories = aiMemoryService.listEffectiveMemories(userId, safeReq.getAgentCode(), agentConfig.getMaxMemoryItems());
            String systemMessage = aiPromptContextBuilder.buildSystemMessage(agentConfig.getSystemPrompt(), memories, safeReq.getContext());
            ChatMemory chatMemory = aiChatMemoryFactory.createMemory(userId, safeReq.getAgentCode(), safeReq.getConversationId(), agentConfig.getMaxContextMessages());
            Map<ToolSpecification, ToolExecutor> tools = aiToolService.buildTools(userId, agentConfig);
            Double temperature = toDouble(agentConfig.getTemperature());
            AiServiceRuntime runtime = aiServiceFactory.createRuntime(agentConfig, llmKey, systemMessage, chatMemory, tools, temperature);

            chatMessageService.saveMessage(userId, safeReq.getConversationId(), "user", safeReq.getMessage(), llmKey.getModelName());

            String modelName = runtime.getModelName();
            Long conversationId = safeReq.getConversationId();
            runtime.getStreamingAssistantService()
                    .chat(safeReq.getMessage())
                    .onPartialResponse(token -> {
                        if (terminated.get()) {
                            return;
                        }
                        try {
                            fullResponse.append(token);
                            sendToken(emitter, token, legacyProtocol);
                        } catch (IOException e) {
                            terminateEmitter(emitter, terminated, "AI streaming client disconnected while sending token");
                        }
                    })
                    .onCompleteResponse(response -> {
                        if (!terminated.compareAndSet(false, true)) {
                            return;
                        }
                        try {
                            chatMessageService.saveMessage(userId, conversationId, "assistant", fullResponse.toString(), modelName);
                            sendDone(emitter, conversationId, modelName, legacyProtocol);
                            emitter.complete();
                        } catch (Exception e) {
                            sendClaimedStreamError(emitter, legacyProtocol, e);
                        }
                    })
                    .onError(error -> sendStreamError(emitter, terminated, legacyProtocol, error))
                    .start();
        } catch (Exception e) {
            sendStreamError(emitter, terminated, legacyProtocol, e);
        }
        return emitter;
    }

    private void sendToken(SseEmitter emitter, String token, boolean legacyProtocol) throws IOException {
        if (legacyProtocol) {
            emitter.send(SseEmitter.event().data(token));
            return;
        }
        emitter.send(SseEmitter.event().name("token").data(Map.of("content", token)));
    }

    private void sendDone(SseEmitter emitter, Long conversationId, String modelName, boolean legacyProtocol) throws IOException {
        if (legacyProtocol) {
            emitter.send(SseEmitter.event().data("[DONE]"));
            return;
        }
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("conversationId", conversationId);
        payload.put("modelName", modelName);
        emitter.send(SseEmitter.event().name("done").data(payload));
    }

    private void sendStreamError(
            SseEmitter emitter,
            AtomicBoolean terminated,
            boolean legacyProtocol,
            Throwable error
    ) {
        if (!terminated.compareAndSet(false, true)) {
            return;
        }
        sendClaimedStreamError(emitter, legacyProtocol, error);
    }

    private void sendClaimedStreamError(SseEmitter emitter, boolean legacyProtocol, Throwable error) {
        log.error("AI streaming generation failed: {}", error.getMessage(), error);
        try {
            if (legacyProtocol) {
                emitter.send(SseEmitter.event().data("[ERROR] " + error.getMessage()));
            } else {
                emitter.send(SseEmitter.event().name("error").data(Map.of(
                        "code", "AI_STREAM_FAILED",
                        "message", "AI 生成失败，请稍后重试"
                )));
            }
            emitter.complete();
        } catch (IOException sendError) {
            log.info("AI streaming client disconnected before error event was delivered");
            emitter.completeWithError(sendError);
        }
    }

    private void terminateEmitter(SseEmitter emitter, AtomicBoolean terminated, String reason) {
        if (terminated.compareAndSet(false, true)) {
            log.info(reason);
            emitter.complete();
        }
    }

    private AiChatReq normalizeRequest(AiChatReq req) {
        AiChatReq safeReq = req == null ? new AiChatReq() : req;
        if (!StringUtils.hasText(safeReq.getAgentCode())) {
            safeReq.setAgentCode(DEFAULT_AGENT_CODE);
        }
        if (safeReq.getMessage() == null) {
            safeReq.setMessage("");
        }
        return safeReq;
    }

    private LLMKeyEntity getDefaultLlmKey(Long userId) {
        LLMKeyEntity llmKey = llmKeyService.getDefaultLLMKey(userId);
        if (llmKey == null) {
            throw new IllegalStateException("请先配置默认大模型 API Key");
        }
        return llmKey;
    }

    private AiAgentConfigVO getEnabledAgentConfig(Long userId, String agentCode) {
        AiAgentConfigVO agentConfig = aiAgentConfigService.getEffectiveConfig(userId, agentCode);
        if (Boolean.FALSE.equals(agentConfig.getEnabled())) {
            throw new IllegalStateException("AI Agent 已禁用: " + agentConfig.getCode());
        }
        return agentConfig;
    }

    private LLMKeyEntity resolveLlmKey(Long userId, String modelKeyId) {
        if (!StringUtils.hasText(modelKeyId)) {
            return getDefaultLlmKey(userId);
        }
        List<LLMKeyEntity> keys = llmKeyService.getLLMKeyList(userId);
        if (keys != null) {
            for (LLMKeyEntity key : keys) {
                if (key != null && Objects.equals(key.getId(), modelKeyId.trim())) {
                    return key;
                }
            }
        }
        throw new IllegalStateException("模型配置不存在或不属于当前用户");
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
