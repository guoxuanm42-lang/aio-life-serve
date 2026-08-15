package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.ArticleSummary;

/**
 * 文章活动统计服务，按统一活动周期分别汇总新增和更新文章。
 *
 * @author Ethan
 * @date 2026-08-13
 */
public interface ArticleActivitySummaryService {

    /**
     * 汇总指定用户在活动周期内新增和更新的文章。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 文章活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    ArticleSummary summarize(Long userId, AiActivityDateRange range);
}
