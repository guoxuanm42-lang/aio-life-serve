package top.aiolife.record.pojo.req;

import lombok.Data;

import java.util.List;

/**
 * 美食记录图片排序请求，用于批量保存图片排序值。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordImageSortReq {

    /**
     * 图片排序项列表。
     */
    private List<Item> items;

    /**
     * 图片排序项。
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Data
    public static class Item {

        /**
         * 图片 ID。
         */
        private Long id;

        /**
         * 排序值。
         */
        private Integer sortOrder;
    }
}
