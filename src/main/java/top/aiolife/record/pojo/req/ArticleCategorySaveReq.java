package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 文章分类保存请求，承载分类名称和排序值。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Data
public class ArticleCategorySaveReq {

    /**
     * 分类 ID，更新和删除时使用。
     */
    private Long id;

    /**
     * 分类名称。
     */
    private String name;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
