package top.aiolife.mcp.handler;

import cn.dev33.satoken.stp.StpUtil;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import top.aiolife.mcp.config.McpServerConfig;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.service.IMcpToolCallService;
import top.aiolife.mcp.service.IMcpToolConfigService;

import java.util.List;

/**
 * MCP tool handler.
 *
 * <p>Builds MCP protocol tool specifications and delegates tool calls to the shared audited call service.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolHandlers {

    private final McpToolRegistry toolRegistry;
    private final IMcpToolConfigService mcpToolConfigService;
    private final IMcpToolCallService mcpToolCallService;

    /**
     * Gets synchronous tool specifications exposed by the MCP server.
     *
     * <p>Disabled tools are filtered out before the MCP protocol tools/list response is built.</p>
     *
     * @return MCP synchronous tool specification list
     *
     * @author Ethan
     * @date 2026-06-03
     */
    public List<McpServerFeatures.SyncToolSpecification> getToolSpecifications() {
        return toolRegistry.getAllTools().stream()
                .filter(definition -> mcpToolConfigService.isToolEnabled(definition.name()))
                .map(definition -> McpServerFeatures.SyncToolSpecification.builder()
                        .tool(definition.schema())
                        .callHandler((exchange, request) -> {
                            Object loginId = currentLoginId(exchange);
                            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                            log.info("[MCP] 用户 {} 调用工具 {}", loginId == null ? "unknown" : loginId, definition.name());
                            return mcpToolCallService.invoke(definition.name(), request.arguments(), loginId, requestAttributes);
                        })
                        .build())
                .toList();
    }

    private Object currentLoginId(McpSyncServerExchange exchange) {
        Object transportLoginId = exchange.transportContext().get(McpServerConfig.LOGIN_ID_CONTEXT_KEY);
        if (transportLoginId != null) {
            return transportLoginId;
        }
        try {
            return StpUtil.getLoginIdDefaultNull();
        } catch (Exception exception) {
            return null;
        }
    }
}
