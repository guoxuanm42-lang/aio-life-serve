package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 闪念行动型结构化详情实体。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Data
@TableName("thought_action_detail")
public class ThoughtActionDetailEntity extends BaseEntity {

    @TableField("thought_id")
    private Long thoughtId;

    private Long userId;

    private String resultSummary;

    private String reflection;

    private String nextAction;

    private String shelveReason;

    private String shelveReasonTag;

    private String restartPolicy;

    private String archiveReason;

    private String valueLevel;

    private String archiveType;
}
