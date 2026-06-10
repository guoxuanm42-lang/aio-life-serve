package top.aiolife.mcp.handler;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.service.IMcpToolCallService;
import top.aiolife.mcp.service.IMcpToolConfigService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MCP tool handler test.
 *
 * @author Ethan
 * @date 2026-06-03
 */
class McpToolHandlersTest {

    /**
     * Verifies disabled tools are not exposed through MCP protocol tools/list.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldHideDisabledToolsFromMcpToolSpecifications() {
        McpToolRegistry registry = mock(McpToolRegistry.class);
        IMcpToolConfigService configService = mock(IMcpToolConfigService.class);
        IMcpToolCallService callService = mock(IMcpToolCallService.class);
        McpToolDefinition enabled = buildDefinition("task_list");
        McpToolDefinition disabled = buildDefinition("thought_save");
        when(registry.getAllTools()).thenReturn(List.of(enabled, disabled));
        when(configService.isToolEnabled("task_list")).thenReturn(true);
        when(configService.isToolEnabled("thought_save")).thenReturn(false);

        McpToolHandlers handlers = new McpToolHandlers(registry, configService, callService);

        assertEquals(1, handlers.getToolSpecifications().size());
    }

    private McpToolDefinition buildDefinition(String name) {
        McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of(),
                List.of(),
                false,
                Map.of(),
                Map.of()
        );
        McpSchema.Tool schema = new McpSchema.Tool(
                name,
                name,
                name,
                inputSchema,
                null,
                new McpSchema.ToolAnnotations(name, false, false, false, false, null),
                null
        );
        return new McpToolDefinition(
                name,
                name,
                new Object(),
                null,
                Object.class,
                Object.class,
                schema,
                true,
                true,
                null,
                null
        );
    }
}
