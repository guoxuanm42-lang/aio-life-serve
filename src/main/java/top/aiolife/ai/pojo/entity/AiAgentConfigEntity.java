package top.aiolife.ai.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Agent 配置实体。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
@TableName("ai_agent_config")
public class AiAgentConfigEntity {

    /**
     * 主键 id。
     */
    @TableId(type = IdType.ASSIGN_ID)
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

    /**
     * 是否启用当前 Agent。
     */
    private Boolean enabled;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记。
     */
    @TableLogic
    private Integer isDeleted;
}
