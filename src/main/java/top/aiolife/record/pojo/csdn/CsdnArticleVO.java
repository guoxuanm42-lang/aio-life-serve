package top.aiolife.record.pojo.csdn;

import lombok.Data;

/**
 * CSDN 文章列表返回对象。
 *
 * @author Ethan
 * @date 2026-06-08
 */
@Data
public class CsdnArticleVO {

    /**
     * 文章标识。
     */
    private String id;

    /**
     * 文章标题。
     */
    private String title;

    /**
     * 文章链接。
     */
    private String url;

    /**
     * 文章摘要。
     */
    private String description;

    /**
     * 发布时间。
     */
    private String postTime;

    /**
     * 阅读数。
     */
    private Long viewCount = 0L;

    /**
     * 评论数。
     */
    private Long commentCount = 0L;

    /**
     * 点赞数。
     */
    private Long likeCount = 0L;

    /**
     * 收藏数。
     */
    private Long collectCount = 0L;
}
