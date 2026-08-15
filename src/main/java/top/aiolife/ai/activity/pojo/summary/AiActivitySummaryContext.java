package top.aiolife.ai.activity.pojo.summary;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 活动总结统一上下文，聚合指定周期内各业务模块的结构化统计数据。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiActivitySummaryContext {

    private String period;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private TimeRecordSummary timeRecord;

    private ThoughtSummary thought;

    private FoodSummary food;

    private TodoSummary todo;

    private ProblemSummary problem;

    private NoteSummary note;

    private AlbumSummary album;

    private ArticleSummary article;

    private McpSummary mcp;
}
