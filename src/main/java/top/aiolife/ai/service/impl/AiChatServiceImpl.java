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

/**
 * 默认 AI 聊天编排服务实现。
 *
 * @author Ethan
 * @date 2026-06-29
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
     * @return SSE 发送器，用于输出模型 token 和完成标记
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Override
    public SseEmitter chatStream(Long userId, AiChatReq req) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        StringBuilder fullResponse = new StringBuilder();

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
                        try {
                            fullResponse.append(token);
                            emitter.send(SseEmitter.event().data(token));
                        } catch (IOException e) {
                            log.error("Failed to send token: {}", e.getMessage());
                            emitter.completeWithError(e);
                        }
                    })
                    .onCompleteResponse(response -> {
                        try {
                            chatMessageService.saveMessage(userId, conversationId, "assistant", fullResponse.toString(), modelName);
                            emitter.send(SseEmitter.event().data("[DONE]"));
                            emitter.complete();
                        } catch (IOException e) {
                            log.error("Failed to send complete event: {}", e.getMessage());
                            emitter.completeWithError(e);
                        }
                    })
                    .onError(error -> {
                        try {
                            emitter.send(SseEmitter.event().data("[ERROR] " + error.getMessage()));
                            emitter.complete();
                        } catch (IOException e) {
                            log.error("Failed to send error event: {}", e.getMessage());
                            emitter.completeWithError(e);
                        }
                    })
                    .start();
        } catch (Exception e) {
            log.error("Failed to start AI streaming chat: {}", e.getMessage(), e);
            try {
                emitter.send(SseEmitter.event().data("[ERROR] " + e.getMessage()));
            } catch (IOException ioException) {
                log.error("Failed to send error event: {}", ioException.getMessage());
            }
            emitter.complete();
        }

        emitter.onTimeout(() -> {
            log.warn("SSE emitter timeout");
            emitter.complete();
        });

        emitter.onCompletion(() -> log.debug("SSE emitter completed"));
        return emitter;
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
