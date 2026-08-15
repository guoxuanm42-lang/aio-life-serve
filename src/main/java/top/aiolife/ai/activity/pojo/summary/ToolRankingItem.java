package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

/**
 * MCP 工具调用排行项，记录单个工具的调用结果和平均耗时。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class ToolRankingItem {

    private String toolName;

    private long callCount;

    private long successCount;

    private long failedCount;

    private long averageDurationMs;
}
