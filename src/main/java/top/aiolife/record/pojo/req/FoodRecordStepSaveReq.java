package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 美食记录步骤保存请求。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordStepSaveReq {

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
