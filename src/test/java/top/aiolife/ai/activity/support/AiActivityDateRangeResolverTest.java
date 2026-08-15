package top.aiolife.ai.activity.support;

import org.junit.jupiter.api.Test;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.model.AiActivitySummaryPeriod;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AI 活动总结时间范围解析器测试。
 *
 * @author Ethan
 * @date 2026-08-12
 */
class AiActivityDateRangeResolverTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    @Test
    void shouldResolveWeekFromMondayForWednesdayRequest() {
        AiActivityDateRange range = resolverAt("2026-08-12T07:30:00Z").resolve("week");

        assertRange(range, AiActivitySummaryPeriod.WEEK,
                LocalDateTime.of(2026, 8, 10, 0, 0),
                LocalDateTime.of(2026, 8, 12, 15, 30),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12));
    }

    @Test
    void shouldKeepMondayMidnightAsWeekBoundary() {
        AiActivityDateRange range = resolverAt("2026-08-09T16:00:00Z").resolve("week");

        assertRange(range, AiActivitySummaryPeriod.WEEK,
                LocalDateTime.of(2026, 8, 10, 0, 0),
                LocalDateTime.of(2026, 8, 10, 0, 0),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 10));
    }

    @Test
    void shouldResolveMonthFromFirstDay() {
        AiActivityDateRange range = resolverAt("2026-08-12T07:30:00Z").resolve("month");

        assertRange(range, AiActivitySummaryPeriod.MONTH,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 12, 15, 30),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 12));
    }

    @Test
    void shouldKeepMonthStartMidnightAsBoundary() {
        AiActivityDateRange range = resolverAt("2026-07-31T16:00:00Z").resolve("month");

        assertEquals(range.getStartTime(), range.getEndTime());
        assertEquals(LocalDate.of(2026, 8, 1), range.getStartDate());
        assertEquals(LocalDate.of(2026, 8, 1), range.getEndDate());
    }

    @Test
    void shouldResolveYearFromJanuaryFirst() {
        AiActivityDateRange range = resolverAt("2026-08-12T07:30:00Z").resolve("year");

        assertRange(range, AiActivitySummaryPeriod.YEAR,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 8, 12, 15, 30),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 8, 12));
    }

    @Test
    void shouldKeepNewYearMidnightAsBoundary() {
        AiActivityDateRange range = resolverAt("2025-12-31T16:00:00Z").resolve("year");

        assertEquals(range.getStartTime(), range.getEndTime());
        assertEquals(LocalDate.of(2026, 1, 1), range.getStartDate());
        assertEquals(LocalDate.of(2026, 1, 1), range.getEndDate());
    }

    @Test
    void shouldAlwaysResolveWithShanghaiBusinessTime() {
        Clock utcClock = Clock.fixed(Instant.parse("2026-08-12T23:30:00Z"), ZoneOffset.UTC);
        AiActivityDateRange range = new AiActivityDateRangeResolver(utcClock).resolve("month");

        assertEquals(LocalDateTime.of(2026, 8, 13, 7, 30), range.getEndTime());
        assertEquals(LocalDate.of(2026, 8, 13), range.getEndDate());
    }

    @Test
    void shouldRejectInvalidPeriodValues() {
        AiActivityDateRangeResolver resolver = resolverAt("2026-08-12T07:30:00Z");

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve((String) null));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(""));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(" "));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("Week"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(" week"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("today"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve((AiActivitySummaryPeriod) null));
    }

    private AiActivityDateRangeResolver resolverAt(String instant) {
        return new AiActivityDateRangeResolver(Clock.fixed(Instant.parse(instant), SHANGHAI));
    }

    private void assertRange(
            AiActivityDateRange range,
            AiActivitySummaryPeriod period,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LocalDate startDate,
            LocalDate endDate
    ) {
        assertEquals(period, range.getPeriod());
        assertEquals(startTime, range.getStartTime());
        assertEquals(endTime, range.getEndTime());
        assertEquals(startDate, range.getStartDate());
        assertEquals(endDate, range.getEndDate());
    }
}
