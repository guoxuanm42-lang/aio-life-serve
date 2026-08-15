package top.aiolife.ai.activity.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 活动统计时间范围，同时提供时间字段范围和时迹业务日期范围。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
@Builder
public class AiActivityDateRange {

    private AiActivitySummaryPeriod period;

    /**
     * 普通时间字段查询的包含起点。
     */
    private LocalDateTime startTime;

    /**
     * 普通时间字段查询的不包含终点。
     */
    private LocalDateTime endTime;

    /**
     * 时迹业务日期查询的包含起始日期。
     */
    private LocalDate startDate;

    /**
     * 时迹业务日期查询的包含结束日期。
     */
    private LocalDate endDate;
}
