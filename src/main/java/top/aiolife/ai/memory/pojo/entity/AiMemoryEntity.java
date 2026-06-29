package top.aiolife.ai.memory.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 长期记忆实体。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
@TableName("ai_memory")
public class AiMemoryEntity {

    /**
     * 主键 id。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 id。
     */
    private Long userId;

    /**
     * Agent 编码。
     */
    private String agentCode;

    /**
     * 记忆类型。
     */
    private String memoryType;

    /**
     * 记忆键。
     */
    private String memoryKey;

    /**
     * 记忆内容。
     */
    private String memoryValue;

    /**
     * 来源类型。
     */
    private String sourceType;

    /**
     * 来源业务 id。
     */
    private String sourceId;

    /**
     * 重要度。
     */
    private Integer importance;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记。
     */
    @TableLogic
    private Integer isDeleted;
}
