package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 活动总结统计，承载调用结果、平均耗时和工具排行。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class McpSummary {

    private long totalCalls;

    private long successCalls;

    private long failedCalls;

    private long averageDurationMs;

    private List<ToolRankingItem> toolRanking = new ArrayList<>();

    /**
     * 判断当前 MCP 统计是否没有可输出数据。
     *
     * @return 没有工具调用时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public boolean isEmpty() {
        return totalCalls == 0;
    }
}
