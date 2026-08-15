package top.aiolife.ai.activity.support;

import java.time.LocalDateTime;

/**
 * AI 活动总结统计策略，统一定义明细上限和文章新增、更新口径。
 *
 * @author Ethan
 * @date 2026-08-12
 */
public final class AiActivitySummaryPolicy {

    public static final int TITLE_DETAIL_LIMIT = 10;

    public static final int MAIN_ACTIVITY_LIMIT = 5;

    public static final int TOOL_RANKING_LIMIT = 5;

    private AiActivitySummaryPolicy() {
    }

    /**
     * 判断文章是否属于统计周期内新增。
     *
     * @param createTime 文章创建时间
     * @param startTime 统计周期包含起点
     * @param endTime 统计周期不包含终点
     * @return 创建时间位于半开区间时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public static boolean isNewArticle(LocalDateTime createTime, LocalDateTime startTime, LocalDateTime endTime) {
        return isInRange(createTime, startTime, endTime);
    }

    /**
     * 判断既有文章是否在统计周期内更新，确保与新增口径互斥。
     *
     * @param createTime 文章创建时间
     * @param updateTime 文章更新时间
     * @param startTime 统计周期包含起点
     * @param endTime 统计周期不包含终点
     * @return 文章在周期前创建且更新时间位于半开区间时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public static boolean isUpdatedArticle(
            LocalDateTime createTime,
            LocalDateTime updateTime,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return createTime != null
                && startTime != null
                && createTime.isBefore(startTime)
                && isInRange(updateTime, startTime, endTime);
    }

    private static boolean isInRange(LocalDateTime value, LocalDateTime startTime, LocalDateTime endTime) {
        return value != null
                && startTime != null
                && endTime != null
                && !value.isBefore(startTime)
                && value.isBefore(endTime);
    }
}
