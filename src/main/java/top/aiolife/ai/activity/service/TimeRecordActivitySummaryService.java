package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.TimeRecordSummary;

/**
 * 时迹活动统计服务，按业务日期汇总用户的时间记录。
 *
 * @author Ethan
 * @date 2026-08-13
 */
public interface TimeRecordActivitySummaryService {

    /**
     * 汇总指定用户在活动日期范围内的时迹记录。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 时迹活动统计结果
     * @throws IllegalArgumentException 用户、时间或业务日期范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    TimeRecordSummary summarize(Long userId, AiActivityDateRange range);
}
