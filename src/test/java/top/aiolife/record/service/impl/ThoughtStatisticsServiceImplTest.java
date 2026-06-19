package top.aiolife.record.service.impl;

import org.junit.jupiter.api.Test;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.pojo.entity.ThoughtEntity;
import top.aiolife.record.pojo.req.ThoughtStatisticsTrendReq;
import top.aiolife.record.pojo.vo.ThoughtStatisticsTrendVO;
import top.aiolife.record.pojo.vo.ThoughtStatisticsVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 闪念统计服务测试。
 *
 * @author Ethan
 * @date 2026-06-13
 */
class ThoughtStatisticsServiceImplTest {

    private final IThoughtMapper thoughtMapper = mock(IThoughtMapper.class);

    private final ThoughtStatisticsServiceImpl service = new ThoughtStatisticsServiceImpl(thoughtMapper);

    @Test
    void shouldReturnCompleteEmptyOverviewWhenNoData() {
        when(thoughtMapper.selectList(any())).thenReturn(List.of());

        ThoughtStatisticsVO result = service.overview(1L);

        assertEquals(0, result.getSummary().getTotalCount());
        assertEquals(0, result.getSummary().getWeekNewCount());
        assertEquals(0, result.getSummary().getMonthNewCount());
        assertEquals(BigDecimal.ZERO.setScale(1), result.getSummary().getConversionRate());
        assertEquals(5, result.getStatusDistribution().size());
        assertEquals(8, result.getCategoryDistribution().size());
        assertEquals(3, result.getTypeSummaries().size());
    }

    @Test
    void shouldAggregateOverviewByStatusCategoryAndType() {
        LocalDateTime now = LocalDateTime.now();
        when(thoughtMapper.selectList(any())).thenReturn(List.of(
                thought("action", "pending", "cyan", now.minusDays(1)),
                thought("action", "ongoing", "green", now.minusDays(2)),
                thought("action", "done", "cyan", now.minusDays(3)),
                thought("emotion", "done", "teal", now.minusDays(4)),
                thought("reflection", "archived", "unknown-key", now.minusDays(5))
        ));

        ThoughtStatisticsVO result = service.overview(1L);

        assertEquals(5, result.getSummary().getTotalCount());
        assertEquals(2, result.getSummary().getDoneCount());
        assertEquals(1, result.getSummary().getArchivedCount());
        assertEquals(2, result.getSummary().getBacklogCount());
        assertEquals(BigDecimal.valueOf(40.0), result.getSummary().getConversionRate());
        assertEquals(3, result.getTypeSummaries().getFirst().getTotalCount());
        assertEquals("unknown", result.getCategoryDistribution().getLast().getKey());
        assertEquals(1, result.getCategoryDistribution().getLast().getCount());
    }

    @Test
    void shouldBuildContinuousSevenDayTrendAndFilterByThoughtType() {
        LocalDate today = LocalDate.now();
        when(thoughtMapper.selectList(any())).thenReturn(List.of(
                thought("action", "done", "cyan", today.minusDays(1).atTime(9, 0)),
                thought("emotion", "done", "green", today.minusDays(1).atTime(10, 0)),
                thought("reflection", "archived", "blue", today.minusDays(2).atTime(11, 0))
        ));
        ThoughtStatisticsTrendReq req = new ThoughtStatisticsTrendReq();
        req.setRange("7d");
        req.setGroupBy("day");
        req.setThoughtType("emotion");

        ThoughtStatisticsTrendVO result = service.trend(1L, req);

        assertEquals("7d", result.getRange());
        assertEquals("day", result.getGroupBy());
        assertEquals(7, result.getTrend().size());
        assertEquals(1, result.getTrend().stream().mapToLong(ThoughtStatisticsTrendVO.Point::getCount).sum());
        assertEquals(1, result.getActivity().stream().mapToLong(ThoughtStatisticsTrendVO.Point::getCount).sum());
        assertTrue(result.getCategoryTrends().stream().allMatch(item -> "生活".equals(item.getCategoryName())));
    }

    @Test
    void shouldFallbackInvalidTrendFiltersWithoutFailing() {
        when(thoughtMapper.selectList(any())).thenReturn(List.of(
                thought("action", "pending", "cyan", LocalDateTime.now())
        ));
        ThoughtStatisticsTrendReq req = new ThoughtStatisticsTrendReq();
        req.setRange("bad-range");
        req.setGroupBy("bad-group");
        req.setCategory("bad-category");
        req.setStatus("bad-status");
        req.setThoughtType("bad-type");

        ThoughtStatisticsTrendVO result = service.trend(1L, req);

        assertEquals("30d", result.getRange());
        assertEquals("day", result.getGroupBy());
        assertEquals(30, result.getTrend().size());
        assertEquals(1, result.getTrend().stream().mapToLong(ThoughtStatisticsTrendVO.Point::getCount).sum());
    }

    private ThoughtEntity thought(String thoughtType, String status, String themeKey, LocalDateTime createTime) {
        ThoughtEntity entity = new ThoughtEntity();
        entity.setUserId(1L);
        entity.setThoughtType(thoughtType);
        entity.setStatus(status);
        entity.setThemeKey(themeKey);
        entity.setCreateTime(createTime);
        entity.setIsDeleted(0);
        return entity;
    }
}
