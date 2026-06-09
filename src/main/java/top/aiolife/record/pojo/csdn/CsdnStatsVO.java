package top.aiolife.record.pojo.csdn;

import lombok.Data;

/**
 * CSDN 用户统计数据返回对象。
 *
 * @author Ethan
 * @date 2026-06-08
 */
@Data
public class CsdnStatsVO {

    /**
     * 总访问量。
     */
    private Long viewCount = 0L;

    /**
     * 原创文章数。
     */
    private Long originalCount = 0L;

    /**
     * 全站排名。
     */
    private Long rank = 0L;

    /**
     * 粉丝数。
     */
    private Long fansCount = 0L;

    /**
     * 获赞数。
     */
    private Long likeCount = 0L;

    /**
     * 评论数。
     */
    private Long commentCount = 0L;
}
