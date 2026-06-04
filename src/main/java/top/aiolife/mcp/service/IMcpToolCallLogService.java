package top.aiolife.mcp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.mcp.pojo.entity.McpToolCallLogEntity;

import java.util.List;
import java.util.Map;

/**
 * MCP tool call audit log service.
 *
 * <p>Records and queries masked MCP tool call audit logs.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
public interface IMcpToolCallLogService extends IService<McpToolCallLogEntity> {

    /**
     * Records one MCP tool call audit log.
     *
     * @param toolName tool name
     * @param userId login user id
     * @param arguments original call arguments
     * @param success whether the call succeeded
     * @param errorMessage error message for failed calls
     * @param durationMs call duration in milliseconds
     * @return saved audit log entity
     *
     * @author Ethan
     * @date 2026-06-03
     */
    McpToolCallLogEntity record(String toolName,
                                Object userId,
                                Map<String, Object> arguments,
                                boolean success,
                                String errorMessage,
                                long durationMs);

    /**
     * Lists recent audit logs for one MCP tool.
     *
     * @param toolName tool name
     * @param limit maximum rows to return
     * @return recent call audit logs ordered by creation time descending
     *
     * @author Ethan
     * @date 2026-06-03
     */
    List<McpToolCallLogEntity> listRecentLogs(String toolName, int limit);
}
