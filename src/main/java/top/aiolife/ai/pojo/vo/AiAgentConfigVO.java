package top.aiolife.ai.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Agent 配置视图对象。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
public class AiAgentConfigVO {

    /**
     * 配置 id。
     */
    private Long id;

    /**
     * 用户 id，0 表示系统默认配置。
     */
    private Long userId;

    /**
     * Agent 编码。
     */
    private String code;

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
     * 启用工具列表 JSON 文本。
     */
    private String enabledTools;

    /**
     * 记忆范围 JSON 文本。
     */
    private String memoryScope;

    /**
     * 最大短期上下文消息数量。
     */
    private Integer maxContextMessages;

    /**
     * 最大记忆条目数量。
     */
    private Integer maxMemoryItems;

    /**
     * 模型温度参数。
     */
    private BigDecimal temperature;

    /**
     * 是否启用当前 Agent。
     */
    private Boolean enabled;

    /**
     * 是否存在用户覆盖配置。
     */
    private Boolean userConfigured;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间。
     */
    private LocalDateTime updateTime;
}
