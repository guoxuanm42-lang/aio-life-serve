package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.ThoughtSummary;

/**
 * 闪念活动统计服务，按统一活动周期汇总用户新增闪念。
 *
 * @author Ethan
 * @date 2026-08-13
 */
public interface ThoughtActivitySummaryService {

    /**
     * 汇总指定用户在活动周期内新增的闪念。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 闪念活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    ThoughtSummary summarize(Long userId, AiActivityDateRange range);
}
