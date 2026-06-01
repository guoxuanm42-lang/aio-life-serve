package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 美食记录步骤实体，保存单次做饭的步骤流程。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("food_record_step")
public class FoodRecordStepEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 美食记录 ID。
     */
    private Long recordId;

    /**
     * 步骤序号。
     */
    private Integer stepNo;

    /**
     * 步骤标题。
     */
    private String title;

    /**
     * 步骤描述。
     */
    private String description;

    /**
     * 步骤耗时（分钟）。
     */
    private Integer durationMinutes;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
