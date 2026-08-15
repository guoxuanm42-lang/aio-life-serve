package top.aiolife.ai.activity.support;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.model.AiActivitySummaryPeriod;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

/**
 * AI 活动总结时间范围解析器，统一计算周、月、年的上海时区范围。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Component
public class AiActivityDateRangeResolver {

    public static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final Clock clock;

    /**
     * 创建时间范围解析器。
     *
     * @param clock AI 活动统计时钟
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public AiActivityDateRangeResolver(@Qualifier("aiActivityClock") Clock clock) {
        this.clock = clock;
    }

    /**
     * 按请求协议值解析统计时间范围。
     *
     * @param periodValue 周期值，仅支持 week、month、year
     * @return 普通数据半开时间范围及包含两端的时迹日期范围
     * @throws IllegalArgumentException 周期值不合法时抛出
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public AiActivityDateRange resolve(String periodValue) {
        return resolve(AiActivitySummaryPeriod.fromValue(periodValue));
    }

    /**
     * 按统计周期解析时间范围。
     *
     * @param period 统计周期
     * @return 普通数据半开时间范围及包含两端的时迹日期范围
     * @throws IllegalArgumentException 周期为空时抛出
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public AiActivityDateRange resolve(AiActivitySummaryPeriod period) {
        if (period == null) {
            throw new IllegalArgumentException("总结周期不能为空");
        }
        LocalDateTime now = LocalDateTime.now(clock.withZone(BUSINESS_ZONE_ID));
        LocalDate currentDate = now.toLocalDate();
        LocalDate startDate = switch (period) {
            case WEEK -> currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> currentDate.withDayOfMonth(1);
            case YEAR -> currentDate.withDayOfYear(1);
        };
        return AiActivityDateRange.builder()
                .period(period)
                .startTime(startDate.atStartOfDay())
                .endTime(now)
                .startDate(startDate)
                .endDate(currentDate)
                .build();
    }
}
