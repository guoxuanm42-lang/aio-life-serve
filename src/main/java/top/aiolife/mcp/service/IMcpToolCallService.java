package top.aiolife.mcp.service;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Map;

/**
 * MCP tool call service.
 *
 * <p>Provides the shared audited and rate-limited tool invocation path for REST and MCP protocol calls.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
public interface IMcpToolCallService {

    /**
     * Invokes one MCP tool with audit logging and rate limiting.
     *
     * @param name tool name
     * @param arguments tool call arguments
     * @param loginId current login user id
     * @param requestAttributes current request attributes
     * @return MCP call result
     *
     * @author Ethan
     * @date 2026-06-03
     */
    McpSchema.CallToolResult invoke(String name,
                                    Map<String, Object> arguments,
                                    Object loginId,
                                    RequestAttributes requestAttributes);
}
