package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 闪念复盘沉淀型结构化详情实体。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Data
@TableName("thought_reflection_detail")
public class ThoughtReflectionDetailEntity extends BaseEntity {

    @TableField("thought_id")
    private Long thoughtId;

    private Long userId;

    private String reflectionSummary;

    private String lessonType;

    private String archiveType;

    private String valueLevel;

    private String improvementAction;

    private String relatedProject;

    private String tags;
}
