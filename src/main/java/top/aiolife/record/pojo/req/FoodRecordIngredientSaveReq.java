package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 美食记录材料保存请求。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordIngredientSaveReq {

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
