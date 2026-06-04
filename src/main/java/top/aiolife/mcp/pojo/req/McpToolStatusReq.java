package top.aiolife.mcp.pojo.req;

import lombok.Data;

/**
 * MCP tool status update request.
 *
 * <p>Receives enablement changes from the management page.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Data
public class McpToolStatusReq {

    /**
     * Whether the tool should be enabled.
     */
    private Boolean enabled;
}
