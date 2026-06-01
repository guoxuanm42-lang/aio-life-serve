package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 美食记录材料实体，保存单次做饭使用的材料项。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("food_record_ingredient")
public class FoodRecordIngredientEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 美食记录 ID。
     */
    private Long recordId;

    /**
     * 材料名称。
     */
    private String name;

    /**
     * 数量。
     */
    private String quantity;

    /**
     * 单位。
     */
    private String unit;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
