package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 美食记录图片更新请求，用于修改图片类型、说明和排序。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordImageUpdateReq {

    /**
     * 图片 ID。
     */
    private Long id;

    /**
     * 图片类型。
     */
    private String imageType;

    /**
     * 图片说明。
     */
    private String caption;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
