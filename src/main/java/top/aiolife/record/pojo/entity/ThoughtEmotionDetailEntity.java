package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 闪念情绪型结构化详情实体。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Data
@TableName("thought_emotion_detail")
public class ThoughtEmotionDetailEntity extends BaseEntity {

    @TableField("thought_id")
    private Long thoughtId;

    private Long userId;

    private String emotionType;

    private Integer emotionIntensity;

    private String emotionTrigger;

    private String emotionNeed;

    private String copingAction;

    private String reflectionSummary;

    private String ignoredReason;
}
