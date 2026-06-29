package top.aiolife.ai.pojo.req;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI Agent 配置保存请求对象。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
public class AiAgentConfigSaveReq {

    /**
     * Agent 展示名称。
     */
    private String name;

    /**
     * Agent 描述。
     */
    private String description;

    /**
     * 当前 Agent 使用的大模型 Key id。
     */
    private String modelKeyId;

    /**
     * 拼接到聊天上下文前的系统提示词。
     */
    private String systemPrompt;

    /**
     * 启用工具列表 JSON 文本，预留给后续阶段使用。
     */
    private String enabledTools;

    /**
     * 记忆范围 JSON 文本，预留给后续阶段使用。
     */
    private String memoryScope;

    /**
     * 最大短期上下文消息数量，预留给后续阶段使用。
     */
    private Integer maxContextMessages;

    /**
     * 最大记忆条目数量，预留给后续阶段使用。
     */
    private Integer maxMemoryItems;

    /**
     * 模型温度参数。
     */
    private BigDecimal temperature;
}
