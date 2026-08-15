package top.aiolife.ai.activity.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.model.AiActivitySummaryPeriod;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryReq;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;
import top.aiolife.ai.activity.pojo.summary.AlbumSummary;
import top.aiolife.ai.activity.pojo.summary.ArticleSummary;
import top.aiolife.ai.activity.pojo.summary.FoodSummary;
import top.aiolife.ai.activity.pojo.summary.McpSummary;
import top.aiolife.ai.activity.pojo.summary.NoteSummary;
import top.aiolife.ai.activity.pojo.summary.ProblemSummary;
import top.aiolife.ai.activity.pojo.summary.ThoughtSummary;
import top.aiolife.ai.activity.pojo.summary.TimeRecordSummary;
import top.aiolife.ai.activity.pojo.summary.TodoSummary;
import top.aiolife.ai.activity.service.AlbumActivitySummaryService;
import top.aiolife.ai.activity.service.ArticleActivitySummaryService;
import top.aiolife.ai.activity.service.FoodActivitySummaryService;
import top.aiolife.ai.activity.service.McpActivitySummaryService;
import top.aiolife.ai.activity.service.NoteActivitySummaryService;
import top.aiolife.ai.activity.service.ProblemActivitySummaryService;
import top.aiolife.ai.activity.service.ThoughtActivitySummaryService;
import top.aiolife.ai.activity.service.TimeRecordActivitySummaryService;
import top.aiolife.ai.activity.service.TodoActivitySummaryService;
import top.aiolife.ai.activity.support.AiActivityDateRangeResolver;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 活动统一聚合服务单元测试，验证周期解析、模块调用、空模块过滤和失败策略。
 *
 * @author Ethan
 * @date 2026-08-13
 */
class AiActivitySummaryServiceImplTest {

    private static final Long USER_ID = 101L;
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 8, 10, 0, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2026, 8, 13, 10, 30);

    @ParameterizedTest
    @ValueSource(strings = {"week", "month", "year"})
    void shouldSupportConfiguredPeriodsAndResolveRangeOnce(String period) {
        Fixture fixture = new Fixture(period);
        fixture.stubEmptyModules();
        AiActivitySummaryReq req = request(period, 999L);

        AiActivitySummaryContext context = fixture.service.summarize(USER_ID, req);

        assertEquals(period, context.getPeriod());
        assertEquals(START_TIME, context.getStartTime());
        assertEquals(END_TIME, context.getEndTime());
        verify(fixture.resolver, times(1)).resolve(period);
        fixture.verifyAllModulesWithSameRange();
    }

    @Test
    void shouldFilterEmptyModulesAndKeepPopulatedModules() {
        Fixture fixture = new Fixture("week");
        fixture.stubEmptyModules();
        TimeRecordSummary timeRecord = new TimeRecordSummary();
        timeRecord.setRecordCount(2);
        timeRecord.setTotalMinutes(90);
        FoodSummary food = new FoodSummary();
        food.setNewCount(1);
        ArticleSummary article = new ArticleSummary();
        article.setUpdatedCount(1);
        when(fixture.timeRecordService.summarize(USER_ID, fixture.range)).thenReturn(timeRecord);
        when(fixture.foodService.summarize(USER_ID, fixture.range)).thenReturn(food);
        when(fixture.articleService.summarize(USER_ID, fixture.range)).thenReturn(article);

        AiActivitySummaryContext context = fixture.service.summarize(USER_ID, request("week", null));

        assertSame(timeRecord, context.getTimeRecord());
        assertSame(food, context.getFood());
        assertSame(article, context.getArticle());
        assertNull(context.getThought());
        assertNull(context.getTodo());
        assertNull(context.getProblem());
        assertNull(context.getNote());
        assertNull(context.getAlbum());
        assertNull(context.getMcp());
    }

    @Test
    void shouldReturnOnlyPeriodAndTimeWhenAllModulesAreEmpty() {
        Fixture fixture = new Fixture("month");
        fixture.stubEmptyModules();

        AiActivitySummaryContext context = fixture.service.summarize(USER_ID, request("month", null));

        assertEquals("month", context.getPeriod());
        assertNull(context.getTimeRecord());
        assertNull(context.getThought());
        assertNull(context.getFood());
        assertNull(context.getTodo());
        assertNull(context.getProblem());
        assertNull(context.getNote());
        assertNull(context.getAlbum());
        assertNull(context.getArticle());
        assertNull(context.getMcp());
    }

    @Test
    void shouldInvokeModulesInFixedOrder() {
        Fixture fixture = new Fixture("week");
        fixture.stubEmptyModules();

        fixture.service.summarize(USER_ID, request("week", null));

        InOrder order = inOrder(fixture.timeRecordService, fixture.thoughtService, fixture.foodService,
                fixture.todoService, fixture.problemService, fixture.noteService, fixture.albumService,
                fixture.articleService, fixture.mcpService);
        order.verify(fixture.timeRecordService).summarize(USER_ID, fixture.range);
        order.verify(fixture.thoughtService).summarize(USER_ID, fixture.range);
        order.verify(fixture.foodService).summarize(USER_ID, fixture.range);
        order.verify(fixture.todoService).summarize(USER_ID, fixture.range);
        order.verify(fixture.problemService).summarize(USER_ID, fixture.range);
        order.verify(fixture.noteService).summarize(USER_ID, fixture.range);
        order.verify(fixture.albumService).summarize(USER_ID, fixture.range);
        order.verify(fixture.articleService).summarize(USER_ID, fixture.range);
        order.verify(fixture.mcpService).summarize(USER_ID, fixture.range);
    }

    @Test
    void shouldRejectNullUserAndRequestBeforeResolvingRange() {
        Fixture fixture = new Fixture("week");

        assertThrows(IllegalArgumentException.class,
                () -> fixture.service.summarize(null, request("week", null)));
        assertThrows(IllegalArgumentException.class,
                () -> fixture.service.summarize(USER_ID, null));
        verify(fixture.resolver, never()).resolve("week");
    }

    @Test
    void shouldPropagateInvalidPeriodFromResolver() {
        Fixture fixture = new Fixture("week");
        when(fixture.resolver.resolve("today")).thenThrow(new IllegalArgumentException("非法周期"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fixture.service.summarize(USER_ID, request("today", null)));

        assertEquals("非法周期", exception.getMessage());
        verify(fixture.timeRecordService, never()).summarize(USER_ID, fixture.range);
    }

    @Test
    void shouldFailWholeAggregationWhenModuleThrows() {
        Fixture fixture = new Fixture("week");
        when(fixture.timeRecordService.summarize(USER_ID, fixture.range)).thenReturn(new TimeRecordSummary());
        when(fixture.thoughtService.summarize(USER_ID, fixture.range))
                .thenThrow(new IllegalStateException("查询失败"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> fixture.service.summarize(USER_ID, request("week", null)));

        assertEquals("查询失败", exception.getMessage());
        verify(fixture.foodService, never()).summarize(USER_ID, fixture.range);
    }

    @Test
    void shouldFailWholeAggregationWhenModuleReturnsNull() {
        Fixture fixture = new Fixture("week");
        when(fixture.timeRecordService.summarize(USER_ID, fixture.range)).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> fixture.service.summarize(USER_ID, request("week", null)));
        verify(fixture.thoughtService, never()).summarize(USER_ID, fixture.range);
    }

    private static AiActivitySummaryReq request(String period, Long conversationId) {
        AiActivitySummaryReq req = new AiActivitySummaryReq();
        req.setPeriod(period);
        req.setConversationId(conversationId);
        return req;
    }

    private static final class Fixture {

        private final AiActivityDateRangeResolver resolver = mock(AiActivityDateRangeResolver.class);
        private final TimeRecordActivitySummaryService timeRecordService = mock(TimeRecordActivitySummaryService.class);
        private final ThoughtActivitySummaryService thoughtService = mock(ThoughtActivitySummaryService.class);
        private final FoodActivitySummaryService foodService = mock(FoodActivitySummaryService.class);
        private final TodoActivitySummaryService todoService = mock(TodoActivitySummaryService.class);
        private final ProblemActivitySummaryService problemService = mock(ProblemActivitySummaryService.class);
        private final NoteActivitySummaryService noteService = mock(NoteActivitySummaryService.class);
        private final AlbumActivitySummaryService albumService = mock(AlbumActivitySummaryService.class);
        private final ArticleActivitySummaryService articleService = mock(ArticleActivitySummaryService.class);
        private final McpActivitySummaryService mcpService = mock(McpActivitySummaryService.class);
        private final AiActivityDateRange range;
        private final AiActivitySummaryServiceImpl service;

        private Fixture(String period) {
            AiActivitySummaryPeriod periodType = AiActivitySummaryPeriod.fromValue(period);
            range = AiActivityDateRange.builder()
                    .period(periodType)
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .startDate(LocalDate.of(2026, 8, 10))
                    .endDate(LocalDate.of(2026, 8, 13))
                    .build();
            when(resolver.resolve(period)).thenReturn(range);
            service = new AiActivitySummaryServiceImpl(resolver, timeRecordService, thoughtService,
                    foodService, todoService, problemService, noteService, albumService,
                    articleService, mcpService);
        }

        private void stubEmptyModules() {
            when(timeRecordService.summarize(USER_ID, range)).thenReturn(new TimeRecordSummary());
            when(thoughtService.summarize(USER_ID, range)).thenReturn(new ThoughtSummary());
            when(foodService.summarize(USER_ID, range)).thenReturn(new FoodSummary());
            when(todoService.summarize(USER_ID, range)).thenReturn(new TodoSummary());
            when(problemService.summarize(USER_ID, range)).thenReturn(new ProblemSummary());
            when(noteService.summarize(USER_ID, range)).thenReturn(new NoteSummary());
            when(albumService.summarize(USER_ID, range)).thenReturn(new AlbumSummary());
            when(articleService.summarize(USER_ID, range)).thenReturn(new ArticleSummary());
            when(mcpService.summarize(USER_ID, range)).thenReturn(new McpSummary());
        }

        private void verifyAllModulesWithSameRange() {
            verify(timeRecordService).summarize(USER_ID, range);
            verify(thoughtService).summarize(USER_ID, range);
            verify(foodService).summarize(USER_ID, range);
            verify(todoService).summarize(USER_ID, range);
            verify(problemService).summarize(USER_ID, range);
            verify(noteService).summarize(USER_ID, range);
            verify(albumService).summarize(USER_ID, range);
            verify(articleService).summarize(USER_ID, range);
            verify(mcpService).summarize(USER_ID, range);
        }
    }
}
