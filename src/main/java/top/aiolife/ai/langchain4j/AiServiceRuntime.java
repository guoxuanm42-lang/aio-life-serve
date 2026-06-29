package top.aiolife.ai.langchain4j;

import lombok.Builder;
import lombok.Data;

/**
 * 单次 AI 服务调用的运行时对象。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
@Builder
public class AiServiceRuntime {

    /**
     * 本次运行使用的 Agent 编码。
     */
    private String agentCode;

    /**
     * 本次运行使用的 Agent 展示名称。
     */
    private String agentName;

    /**
     * 本次运行使用的模型名称。
     */
    private String modelName;

    /**
     * 是否已应用 Agent 系统提示词。
     */
    private Boolean systemPromptApplied;

    /**
     * 非流式通用助手服务。
     */
    private GenericAssistantService assistantService;

    /**
     * 流式通用助手服务。
     */
    private GenericStreamingAssistantService streamingAssistantService;
}
