package top.aiolife.mcp.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MCP tool call audit log entity.
 *
 * <p>Stores traceable call records for REST simulation and MCP protocol invocations.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Data
@TableName("mcp_tool_call_log")
public class McpToolCallLogEntity {

    /**
     * Primary key.
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * MCP tool name.
     */
    private String toolName;

    /**
     * Login user id.
     */
    private Long userId;

    /**
     * Masked argument summary.
     */
    private String argumentsSummary;

    /**
     * Whether the call succeeded.
     */
    private Boolean success;

    /**
     * Error message for failed calls.
     */
    private String errorMessage;

    /**
     * Call duration in milliseconds.
     */
    private Long durationMs;

    /**
     * Creation time.
     */
    private LocalDateTime createTime;
}
