package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.FoodSummary;

/**
 * 美食活动统计服务，按统一活动周期汇总用户新增美食记录。
 *
 * @author Ethan
 * @date 2026-08-13
 */
public interface FoodActivitySummaryService {

    /**
     * 汇总指定用户在活动周期内新增的美食记录。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 美食活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    FoodSummary summarize(Long userId, AiActivityDateRange range);
}
