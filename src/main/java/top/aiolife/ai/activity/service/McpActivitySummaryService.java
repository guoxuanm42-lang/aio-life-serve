package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.McpSummary;

/**
 * MCP 活动统计服务，按调用日志汇总用户的工具使用情况。
 *
 * @author Ethan
 * @date 2026-08-13
 */
public interface McpActivitySummaryService {

    /**
     * 汇总指定用户在活动时间范围内的 MCP 工具调用。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return MCP 活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    McpSummary summarize(Long userId, AiActivityDateRange range);
}
