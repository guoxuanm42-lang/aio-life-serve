package top.aiolife.ai.activity.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.aiolife.ai.activity.model.AiActivityDateRange;
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
import top.aiolife.ai.activity.service.AiActivitySummaryService;
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

import java.util.function.Supplier;

/**
 * AI 活动统一聚合服务实现，顺序调用九个统计模块并过滤空模块。
 *
 * @author Ethan
 * @date 2026-08-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiActivitySummaryServiceImpl implements AiActivitySummaryService {

    private final AiActivityDateRangeResolver dateRangeResolver;
    private final TimeRecordActivitySummaryService timeRecordSummaryService;
    private final ThoughtActivitySummaryService thoughtSummaryService;
    private final FoodActivitySummaryService foodSummaryService;
    private final TodoActivitySummaryService todoSummaryService;
    private final ProblemActivitySummaryService problemSummaryService;
    private final NoteActivitySummaryService noteSummaryService;
    private final AlbumActivitySummaryService albumSummaryService;
    private final ArticleActivitySummaryService articleSummaryService;
    private final McpActivitySummaryService mcpSummaryService;

    /**
     * 聚合指定用户在所选周期内的全部活动统计。
     *
     * @param userId 当前用户 ID
     * @param req AI 活动总结请求
     * @return 统一活动统计上下文
     * @throws IllegalArgumentException 用户、请求或周期参数无效时抛出
     * @throws IllegalStateException 任一模块未返回有效统计对象时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    @Override
    @Transactional(readOnly = true)
    public AiActivitySummaryContext summarize(Long userId, AiActivitySummaryReq req) {
        if (userId == null) {
            throw new IllegalArgumentException("用户 ID 不能为空");
        }
        if (req == null) {
            throw new IllegalArgumentException("活动总结请求不能为空");
        }

        long totalStart = System.nanoTime();
        AiActivityDateRange range = dateRangeResolver.resolve(req.getPeriod());
        AiActivitySummaryContext context = initializeContext(range);
        int outputModuleCount = 0;

        TimeRecordSummary timeRecord = invokeModule("timeRecord", () -> timeRecordSummaryService.summarize(userId, range));
        if (!timeRecord.isEmpty()) {
            context.setTimeRecord(timeRecord);
            outputModuleCount++;
        }
        ThoughtSummary thought = invokeModule("thought", () -> thoughtSummaryService.summarize(userId, range));
        if (!thought.isEmpty()) {
            context.setThought(thought);
            outputModuleCount++;
        }
        FoodSummary food = invokeModule("food", () -> foodSummaryService.summarize(userId, range));
        if (!food.isEmpty()) {
            context.setFood(food);
            outputModuleCount++;
        }
        TodoSummary todo = invokeModule("todo", () -> todoSummaryService.summarize(userId, range));
        if (!todo.isEmpty()) {
            context.setTodo(todo);
            outputModuleCount++;
        }
        ProblemSummary problem = invokeModule("problem", () -> problemSummaryService.summarize(userId, range));
        if (!problem.isEmpty()) {
            context.setProblem(problem);
            outputModuleCount++;
        }
        NoteSummary note = invokeModule("note", () -> noteSummaryService.summarize(userId, range));
        if (!note.isEmpty()) {
            context.setNote(note);
            outputModuleCount++;
        }
        AlbumSummary album = invokeModule("album", () -> albumSummaryService.summarize(userId, range));
        if (!album.isEmpty()) {
            context.setAlbum(album);
            outputModuleCount++;
        }
        ArticleSummary article = invokeModule("article", () -> articleSummaryService.summarize(userId, range));
        if (!article.isEmpty()) {
            context.setArticle(article);
            outputModuleCount++;
        }
        McpSummary mcp = invokeModule("mcp", () -> mcpSummaryService.summarize(userId, range));
        if (!mcp.isEmpty()) {
            context.setMcp(mcp);
            outputModuleCount++;
        }

        log.info("AI activity summary completed: userId={}, period={}, outputModules={}, elapsedMs={}",
                userId, context.getPeriod(), outputModuleCount, elapsedMillis(totalStart));
        return context;
    }

    private AiActivitySummaryContext initializeContext(AiActivityDateRange range) {
        if (range == null || range.getPeriod() == null || range.getStartTime() == null || range.getEndTime() == null) {
            throw new IllegalStateException("活动统计时间范围解析结果不完整");
        }
        AiActivitySummaryContext context = new AiActivitySummaryContext();
        context.setPeriod(range.getPeriod().value());
        context.setStartTime(range.getStartTime());
        context.setEndTime(range.getEndTime());
        return context;
    }

    private <T> T invokeModule(String module, Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            T result = supplier.get();
            if (result == null) {
                throw new IllegalStateException(module + " 活动统计结果不能为空");
            }
            log.info("AI activity summary module completed: module={}, elapsedMs={}", module, elapsedMillis(start));
            return result;
        } catch (RuntimeException exception) {
            log.error("AI activity summary module failed: module={}, elapsedMs={}", module, elapsedMillis(start), exception);
            throw exception;
        }
    }

    private long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000L;
    }
}
