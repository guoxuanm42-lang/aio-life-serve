package top.aiolife.ai.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.model.AiActivitySummaryModelResult;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;
import top.aiolife.ai.activity.service.AiActivitySummaryModelService;
import top.aiolife.ai.activity.support.AiActivitySummaryPromptBuilder;
import top.aiolife.ai.langchain4j.AiServiceFactory;
import top.aiolife.ai.langchain4j.AiServiceRuntime;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.ai.service.AiAgentConfigService;
import top.aiolife.llm.pojo.entity.LLMKeyEntity;
import top.aiolife.llm.service.LLMKeyService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI 活动总结模型服务实现，仅注入结构化统计，不加载聊天记忆、长期记忆或 MCP 工具。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Service
@RequiredArgsConstructor
public class AiActivitySummaryModelServiceImpl implements AiActivitySummaryModelService {

    private static final double SUMMARY_TEMPERATURE = 0.2D;
    private final AiAgentConfigService aiAgentConfigService;
    private final LLMKeyService llmKeyService;
    private final AiServiceFactory aiServiceFactory;
    private final AiActivitySummaryPromptBuilder promptBuilder;

    /**
     * 使用会话 Agent 和低温度模型生成活动总结。
     *
     * @param userId 当前用户 ID
     * @param agentCode 会话绑定的 Agent 编码
     * @param context 活动统计上下文
     * @param userMessage 固定用户总结指令
     * @return 模型回复及实际运行信息
     *
     * @author Ethan
     * @date 2026-08-14
     */
    @Override
    public AiActivitySummaryModelResult generate(Long userId, String agentCode,
                                                 AiActivitySummaryContext context, String userMessage) {
        AiAgentConfigVO agent = aiAgentConfigService.getEffectiveConfig(userId, agentCode);
        if (agent == null || Boolean.FALSE.equals(agent.getEnabled())) {
            throw new IllegalStateException("AI Agent 不存在或已禁用");
        }
        LLMKeyEntity llmKey = resolveLlmKey(userId, agent.getModelKeyId());
        String systemMessage = promptBuilder.buildSystemMessage(agent.getSystemPrompt(), context);
        AiServiceRuntime runtime = aiServiceFactory.createRuntime(
                agent, llmKey, systemMessage, null, Map.of(), SUMMARY_TEMPERATURE);
        String content = runtime.getAssistantService().chat(userMessage);
        if (!StringUtils.hasText(content)) {
            throw new IllegalStateException("模型未返回活动总结内容");
        }
        return AiActivitySummaryModelResult.builder()
                .content(content)
                .modelName(runtime.getModelName())
                .agentCode(runtime.getAgentCode())
                .agentName(runtime.getAgentName())
                .build();
    }

    private LLMKeyEntity resolveLlmKey(Long userId, String modelKeyId) {
        if (!StringUtils.hasText(modelKeyId)) {
            LLMKeyEntity defaultKey = llmKeyService.getDefaultLLMKey(userId);
            if (defaultKey == null) {
                throw new IllegalStateException("请先配置默认大模型 API Key");
            }
            return defaultKey;
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
}
