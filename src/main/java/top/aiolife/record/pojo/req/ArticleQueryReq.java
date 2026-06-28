package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 文章分页查询请求，承载关键词、分类、状态和标签等筛选条件。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Data
public class ArticleQueryReq {

    /**
     * 当前页码。
     */
    private Integer page = 1;

    /**
     * 每页数量。
     */
    private Integer pageSize = 50;

    /**
     * 关键词，匹配标题、摘要、纯文本内容和标签。
     */
    private String keyword;

    /**
     * 分类 ID。
     */
    private Long categoryId;

    /**
     * 是否只查询未分类文章。
     */
    private Boolean uncategorized;

    /**
     * 状态。
     */
    private String status;

    /**
     * 标签关键词。
     */
    private String tags;
}
