package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.McpSummary;
import top.aiolife.ai.activity.pojo.summary.ToolRankingItem;
import top.aiolife.ai.activity.service.McpActivitySummaryService;
import top.aiolife.ai.activity.support.AiActivitySummaryPolicy;
import top.aiolife.mcp.mapper.IMcpToolCallLogMapper;
import top.aiolife.mcp.pojo.entity.McpToolCallLogEntity;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 活动统计服务实现，按用户调用日志汇总结果、有效耗时和工具排行。
 *
 * @author Ethan
 * @date 2026-08-13
 */
@Service
@RequiredArgsConstructor
public class McpActivitySummaryServiceImpl implements McpActivitySummaryService {

    private static final String UNKNOWN_TOOL_NAME = "未知工具";

    private final IMcpToolCallLogMapper mcpToolCallLogMapper;

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
    @Override
    public McpSummary summarize(Long userId, AiActivityDateRange range) {
        ActivitySummarySupport.validate(userId, range);
        List<McpToolCallLogEntity> logs = listLogs(userId, range);

        McpSummary summary = new McpSummary();
        summary.setTotalCalls(logs.size());
        summary.setSuccessCalls(logs.stream().filter(item -> Boolean.TRUE.equals(item.getSuccess())).count());
        summary.setFailedCalls(summary.getTotalCalls() - summary.getSuccessCalls());
        summary.setAverageDurationMs(averageDuration(logs));
        summary.setToolRanking(buildToolRanking(logs));
        return summary;
    }

    private List<McpToolCallLogEntity> listLogs(Long userId, AiActivityDateRange range) {
        LambdaQueryWrapper<McpToolCallLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(McpToolCallLogEntity::getToolName, McpToolCallLogEntity::getSuccess,
                McpToolCallLogEntity::getDurationMs, McpToolCallLogEntity::getCreateTime);
        wrapper.eq(McpToolCallLogEntity::getUserId, userId);
        wrapper.ge(McpToolCallLogEntity::getCreateTime, range.getStartTime());
        wrapper.lt(McpToolCallLogEntity::getCreateTime, range.getEndTime());
        return mcpToolCallLogMapper.selectList(wrapper);
    }

    private long averageDuration(List<McpToolCallLogEntity> logs) {
        long validCount = logs.stream().filter(item -> isValidDuration(item.getDurationMs())).count();
        if (validCount == 0) {
            return 0L;
        }
        long totalDuration = logs.stream()
                .filter(item -> isValidDuration(item.getDurationMs()))
                .mapToLong(McpToolCallLogEntity::getDurationMs)
                .sum();
        return Math.round((double) totalDuration / validCount);
    }

    private List<ToolRankingItem> buildToolRanking(List<McpToolCallLogEntity> logs) {
        Map<String, ToolAccumulator> accumulators = new LinkedHashMap<>();
        logs.forEach(log -> accumulators.computeIfAbsent(normalizeToolName(log.getToolName()), key -> new ToolAccumulator())
                .add(log));
        return accumulators.entrySet().stream()
                .map(entry -> toRankingItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(ToolRankingItem::getCallCount).reversed()
                        .thenComparing(ToolRankingItem::getToolName))
                .limit(AiActivitySummaryPolicy.TOOL_RANKING_LIMIT)
                .toList();
    }

    private ToolRankingItem toRankingItem(String toolName, ToolAccumulator accumulator) {
        ToolRankingItem item = new ToolRankingItem();
        item.setToolName(toolName);
        item.setCallCount(accumulator.callCount);
        item.setSuccessCount(accumulator.successCount);
        item.setFailedCount(accumulator.callCount - accumulator.successCount);
        item.setAverageDurationMs(accumulator.validDurationCount == 0
                ? 0L : Math.round((double) accumulator.totalValidDuration / accumulator.validDurationCount));
        return item;
    }

    private String normalizeToolName(String toolName) {
        return StringUtils.hasText(toolName) ? toolName.trim() : UNKNOWN_TOOL_NAME;
    }

    private boolean isValidDuration(Long durationMs) {
        return durationMs != null && durationMs >= 0;
    }

    /**
     * 单个 MCP 工具的内部累计状态。
     *
     * @author Ethan
     * @date 2026-08-13
     */
    private static final class ToolAccumulator {

        private long callCount;
        private long successCount;
        private long validDurationCount;
        private long totalValidDuration;

        private void add(McpToolCallLogEntity log) {
            callCount++;
            if (Boolean.TRUE.equals(log.getSuccess())) {
                successCount++;
            }
            if (log.getDurationMs() != null && log.getDurationMs() >= 0) {
                validDurationCount++;
                totalValidDuration += log.getDurationMs();
            }
        }
    }
}
