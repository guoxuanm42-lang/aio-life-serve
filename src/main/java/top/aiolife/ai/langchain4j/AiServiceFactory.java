package top.aiolife.ai.langchain4j;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.llm.pojo.entity.LLMKeyEntity;

import java.util.Map;

/**
 * 配置化 LangChain4j AI Services 工厂。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Component
@RequiredArgsConstructor
public class AiServiceFactory {

    private final AiModelFactory aiModelFactory;

    /**
     * 为单次聊天请求创建 AI Services 运行时对象。
     *
     * @param agentConfig 生效的 Agent 配置
     * @param llmKey 本次使用的大模型 Key
     * @param systemMessage 生效的系统消息，包含 Agent 提示词和兼容旧逻辑的上下文
     * @param temperature 可选的模型温度参数
     * @return 已配置的 AI 服务运行时对象
     *
     * @author Ethan
     * @date 2026-06-28
     */
    public AiServiceRuntime createRuntime(
            AiAgentConfigVO agentConfig,
            LLMKeyEntity llmKey,
            String systemMessage,
            ChatMemory chatMemory,
            Map<ToolSpecification, ToolExecutor> tools,
            Double temperature
    ) {
        GenericAssistantService assistantService = buildAssistantService(llmKey, systemMessage, chatMemory, tools, temperature);
        GenericStreamingAssistantService streamingAssistantService = buildStreamingAssistantService(llmKey, systemMessage, chatMemory, tools, temperature);

        return AiServiceRuntime.builder()
                .agentCode(agentConfig.getCode())
                .agentName(agentConfig.getName())
                .modelName(llmKey.getModelName())
                .systemPromptApplied(StringUtils.hasText(agentConfig.getSystemPrompt()))
                .assistantService(assistantService)
                .streamingAssistantService(streamingAssistantService)
                .build();
    }

    private GenericAssistantService buildAssistantService(
            LLMKeyEntity llmKey,
            String systemMessage,
            ChatMemory chatMemory,
            Map<ToolSpecification, ToolExecutor> tools,
            Double temperature
    ) {
        var builder = AiServices.builder(GenericAssistantService.class)
                .chatModel(aiModelFactory.createChatModel(
                        llmKey.getApiKey(),
                        llmKey.getBaseUrl(),
                        llmKey.getModelName(),
                        temperature
                ));
        if (StringUtils.hasText(systemMessage)) {
            builder.systemMessage(systemMessage);
        }
        if (chatMemory != null) {
            builder.chatMemory(chatMemory);
        }
        if (tools != null && !tools.isEmpty()) {
            builder.tools(tools);
        }
        return builder.build();
    }

    private GenericStreamingAssistantService buildStreamingAssistantService(
            LLMKeyEntity llmKey,
            String systemMessage,
            ChatMemory chatMemory,
            Map<ToolSpecification, ToolExecutor> tools,
            Double temperature
    ) {
        var builder = AiServices.builder(GenericStreamingAssistantService.class)
                .streamingChatModel(aiModelFactory.createStreamingChatModel(
                        llmKey.getApiKey(),
                        llmKey.getBaseUrl(),
                        llmKey.getModelName(),
                        temperature
                ));
        if (StringUtils.hasText(systemMessage)) {
            builder.systemMessage(systemMessage);
        }
        if (chatMemory != null) {
            builder.chatMemory(chatMemory);
        }
        if (tools != null && !tools.isEmpty()) {
            builder.tools(tools);
        }
        return builder.build();
    }
}
