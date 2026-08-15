package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.model.AiActivitySummaryPeriod;
import top.aiolife.ai.activity.pojo.summary.McpSummary;
import top.aiolife.ai.activity.pojo.summary.TimeRecordSummary;
import top.aiolife.mcp.mapper.IMcpToolCallLogMapper;
import top.aiolife.mcp.pojo.entity.McpToolCallLogEntity;
import top.aiolife.record.mapper.ITimeRecordMapper;
import top.aiolife.record.mapper.ITimeTrackerCategoryMapper;
import top.aiolife.record.pojo.entity.TimeRecordEntity;
import top.aiolife.record.pojo.entity.entity.TimeTrackerCategoryEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 时迹与 MCP 活动统计服务单元测试，验证业务日期、分类覆盖、耗时聚合和工具排行口径。
 *
 * @author Ethan
 * @date 2026-08-13
 */
class TimeRecordAndMcpActivitySummaryServiceTest {

    private static final Long USER_ID = 101L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 8, 10);
    private static final LocalDate END_DATE = LocalDate.of(2026, 8, 13);
    private static final LocalDateTime START_TIME = START_DATE.atStartOfDay();
    private static final LocalDateTime END_TIME = LocalDateTime.of(2026, 8, 13, 10, 30);
    private static final AiActivityDateRange RANGE = AiActivityDateRange.builder()
            .period(AiActivitySummaryPeriod.WEEK)
            .startTime(START_TIME)
            .endTime(END_TIME)
            .startDate(START_DATE)
            .endDate(END_DATE)
            .build();

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, TimeRecordEntity.class);
        TableInfoHelper.initTableInfo(assistant, TimeTrackerCategoryEntity.class);
        TableInfoHelper.initTableInfo(assistant, McpToolCallLogEntity.class);
    }

    @Test
    void shouldSummarizeTimeRecordsWithCategoryOverrideAndUncategorizedFallback() {
        ITimeRecordMapper recordMapper = mock(ITimeRecordMapper.class);
        ITimeTrackerCategoryMapper categoryMapper = mock(ITimeTrackerCategoryMapper.class);
        when(recordMapper.selectList(any())).thenReturn(List.of(
                timeRecord("开发", "1", END_DATE, 60),
                timeRecord("生活", "2", END_DATE.minusDays(1), 30),
                timeRecord(" ", "invalid", END_DATE, 20),
                timeRecord("无效时长", "1", END_DATE, -1),
                timeRecord("核心开发", "1", END_DATE.minusDays(1), 90)));
        TimeTrackerCategoryEntity publicCategory = category(1L, 0L, null, "工作");
        TimeTrackerCategoryEntity privateCategory = category(2L, USER_ID, null, "生活");
        TimeTrackerCategoryEntity override = category(20L, USER_ID, 1L, "专注开发");
        when(categoryMapper.selectList(any())).thenReturn(
                List.of(publicCategory, privateCategory),
                List.of(override));

        TimeRecordSummary summary = new TimeRecordActivitySummaryServiceImpl(recordMapper, categoryMapper)
                .summarize(USER_ID, RANGE);

        assertEquals(5, summary.getRecordCount());
        assertEquals(200, summary.getTotalMinutes());
        assertEquals(3, summary.getCategoryDurations().size());
        assertEquals("1", summary.getCategoryDurations().get(0).getCategoryId());
        assertEquals("专注开发", summary.getCategoryDurations().get(0).getCategoryName());
        assertEquals(150, summary.getCategoryDurations().get(0).getDurationMinutes());
        assertEquals(new BigDecimal("75.00"), summary.getCategoryDurations().get(0).getPercentage());
        assertEquals(List.of("核心开发", "开发", "生活"),
                summary.getMainActivities().stream().map(item -> item.getTitle()).toList());
        assertEquals("专注开发", summary.getMainActivities().get(0).getCategoryName());
        verify(categoryMapper, times(2)).selectList(any());
    }

    @Test
    void shouldUseInclusiveBusinessDateRangeForTimeRecords() {
        ITimeRecordMapper recordMapper = mock(ITimeRecordMapper.class);
        ITimeTrackerCategoryMapper categoryMapper = mock(ITimeTrackerCategoryMapper.class);
        when(recordMapper.selectList(any())).thenReturn(List.of());

        new TimeRecordActivitySummaryServiceImpl(recordMapper, categoryMapper).summarize(USER_ID, RANGE);

        ArgumentCaptor<LambdaQueryWrapper<TimeRecordEntity>> captor = wrapperCaptor();
        verify(recordMapper).selectList(captor.capture());
        captor.getValue().getSqlSegment();
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(USER_ID));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(START_DATE));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(END_DATE));
    }

    @Test
    void shouldLimitMainActivitiesToFiveWithoutChangingTotalTime() {
        ITimeRecordMapper recordMapper = mock(ITimeRecordMapper.class);
        ITimeTrackerCategoryMapper categoryMapper = mock(ITimeTrackerCategoryMapper.class);
        List<TimeRecordEntity> records = java.util.stream.IntStream.rangeClosed(1, 7)
                .mapToObj(index -> timeRecord("活动" + index, null, END_DATE, index * 10))
                .toList();
        when(recordMapper.selectList(any())).thenReturn(records);

        TimeRecordSummary summary = new TimeRecordActivitySummaryServiceImpl(recordMapper, categoryMapper)
                .summarize(USER_ID, RANGE);

        assertEquals(280, summary.getTotalMinutes());
        assertEquals(5, summary.getMainActivities().size());
        assertEquals("活动7", summary.getMainActivities().get(0).getTitle());
    }

    @Test
    void shouldRejectMissingBusinessDateRange() {
        ITimeRecordMapper recordMapper = mock(ITimeRecordMapper.class);
        ITimeTrackerCategoryMapper categoryMapper = mock(ITimeTrackerCategoryMapper.class);
        AiActivityDateRange missingDates = AiActivityDateRange.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> new TimeRecordActivitySummaryServiceImpl(recordMapper, categoryMapper)
                        .summarize(USER_ID, missingDates));
    }

    @Test
    void shouldSummarizeMcpResultsDurationsAndLimitedToolRanking() {
        IMcpToolCallLogMapper mapper = mock(IMcpToolCallLogMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(
                log("tool_a", true, 100L),
                log("tool_a", true, 300L),
                log("tool_b", false, 50L),
                log(" ", null, -1L),
                log("tool_c", true, null),
                log("tool_d", true, 0L),
                log("tool_e", true, 10L),
                log("tool_f", false, 20L)));

        McpSummary summary = new McpActivitySummaryServiceImpl(mapper).summarize(USER_ID, RANGE);

        assertEquals(8, summary.getTotalCalls());
        assertEquals(5, summary.getSuccessCalls());
        assertEquals(3, summary.getFailedCalls());
        assertEquals(80, summary.getAverageDurationMs());
        assertEquals(5, summary.getToolRanking().size());
        assertEquals("tool_a", summary.getToolRanking().get(0).getToolName());
        assertEquals(2, summary.getToolRanking().get(0).getCallCount());
        assertEquals(200, summary.getToolRanking().get(0).getAverageDurationMs());
    }

    @Test
    void shouldUseHalfOpenTimeRangeForMcpLogs() {
        IMcpToolCallLogMapper mapper = mock(IMcpToolCallLogMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());

        McpSummary summary = new McpActivitySummaryServiceImpl(mapper).summarize(USER_ID, RANGE);

        assertEquals(0, summary.getTotalCalls());
        assertEquals(List.of(), summary.getToolRanking());
        ArgumentCaptor<LambdaQueryWrapper<McpToolCallLogEntity>> captor = wrapperCaptor();
        verify(mapper).selectList(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("create_time"));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(USER_ID));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(START_TIME));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(END_TIME));
    }

    private static TimeRecordEntity timeRecord(String title, String categoryId, LocalDate date, Integer duration) {
        TimeRecordEntity entity = new TimeRecordEntity();
        entity.setTitle(title);
        entity.setCategoryId(categoryId);
        entity.setDate(date);
        entity.setDuration(duration);
        return entity;
    }

    private static TimeTrackerCategoryEntity category(Long id, Long userId, Long templateId, String name) {
        TimeTrackerCategoryEntity entity = new TimeTrackerCategoryEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setTemplateId(templateId);
        entity.setName(name);
        return entity;
    }

    private static McpToolCallLogEntity log(String toolName, Boolean success, Long durationMs) {
        McpToolCallLogEntity entity = new McpToolCallLogEntity();
        entity.setToolName(toolName);
        entity.setSuccess(success);
        entity.setDurationMs(durationMs);
        return entity;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> ArgumentCaptor<LambdaQueryWrapper<T>> wrapperCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(LambdaQueryWrapper.class);
    }
}
