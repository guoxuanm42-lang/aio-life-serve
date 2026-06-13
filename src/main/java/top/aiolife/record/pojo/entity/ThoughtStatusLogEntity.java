package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 闪念状态流转日志实体。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Data
@TableName("thought_status_log")
public class ThoughtStatusLogEntity extends BaseEntity {

    @TableField("thought_id")
    private Long thoughtId;

    @TableField("user_id")
    private Long userId;

    @TableField("thought_type")
    private String thoughtType;

    @TableField("from_status")
    private String fromStatus;

    @TableField("to_status")
    private String toStatus;

    @TableField("change_reason")
    private String changeReason;
}
