package top.aiolife.mcp.service.impl;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.invoker.McpToolInvoker;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.service.IMcpToolCallLogService;
import top.aiolife.mcp.service.IMcpToolConfigService;
import top.aiolife.record.util.RedisUtil;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MCP tool call service implementation test.
 *
 * @author Ethan
 * @date 2026-06-03
 */
class McpToolCallServiceImplTest {

    /**
     * Verifies successful calls invoke the tool and record success audit logs.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldInvokeToolAndRecordSuccessLog() {
        Fixture fixture = buildFixture();
        Map<String, Object> arguments = Map.of("title", "demo");
        McpSchema.CallToolResult success = McpSchema.CallToolResult.builder()
                .addTextContent("ok")
                .isError(false)
                .build();
        when(fixture.registry.getTool("tool_save")).thenReturn(fixture.definition);
        when(fixture.configService.isToolEnabled("tool_save")).thenReturn(true);
        when(fixture.redisUtil.increment("mcp:tool:rate:1001:tool_save", 1)).thenReturn(1L);
        when(fixture.invoker.invoke(fixture.definition, arguments, 1001L, null)).thenReturn(success);

        McpSchema.CallToolResult result = fixture.service.invoke("tool_save", arguments, 1001L, null);

        assertFalse(result.isError());
        verify(fixture.logService).record(eq("tool_save"), eq(1001L), eq(arguments), eq(true), isNull(), anyLong());
    }

    /**
     * Verifies tool business errors are recorded as failed audit logs.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldRecordFailureWhenToolReturnsError() {
        Fixture fixture = buildFixture();
        Map<String, Object> arguments = Map.of("title", "demo");
        McpSchema.CallToolResult failure = McpSchema.CallToolResult.builder()
                .addTextContent("业务失败")
                .isError(true)
                .build();
        when(fixture.registry.getTool("tool_save")).thenReturn(fixture.definition);
        when(fixture.configService.isToolEnabled("tool_save")).thenReturn(true);
        when(fixture.redisUtil.increment("mcp:tool:rate:1001:tool_save", 1)).thenReturn(1L);
        when(fixture.invoker.invoke(fixture.definition, arguments, 1001L, null)).thenReturn(failure);

        McpSchema.CallToolResult result = fixture.service.invoke("tool_save", arguments, 1001L, null);

        assertTrue(result.isError());
        verify(fixture.logService).record(eq("tool_save"), eq(1001L), eq(arguments), eq(false), eq("业务失败"), anyLong());
    }

    /**
     * Verifies missing runtime tools are rejected and recorded without invoking the tool.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldRejectMissingToolAndRecordFailure() {
        Fixture fixture = buildFixture();
        Map<String, Object> arguments = Map.of("title", "demo");
        when(fixture.registry.getTool("missing_tool")).thenReturn(null);

        McpSchema.CallToolResult result = fixture.service.invoke("missing_tool", arguments, 1001L, null);

        assertTrue(result.isError());
        verify(fixture.invoker, never()).invoke(any(), any(), any(), any());
        verify(fixture.logService).record(eq("missing_tool"), eq(1001L), eq(arguments), eq(false), eq("MCP 工具不存在: missing_tool"), anyLong());
    }

    /**
     * Verifies disabled tools are rejected and recorded without invoking the tool.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldRejectDisabledToolAndRecordFailure() {
        Fixture fixture = buildFixture();
        Map<String, Object> arguments = Map.of("title", "demo");
        when(fixture.registry.getTool("tool_save")).thenReturn(fixture.definition);
        when(fixture.configService.isToolEnabled("tool_save")).thenReturn(false);

        McpSchema.CallToolResult result = fixture.service.invoke("tool_save", arguments, 1001L, null);

        assertTrue(result.isError());
        verify(fixture.invoker, never()).invoke(any(), any(), any(), any());
        verify(fixture.logService).record(eq("tool_save"), eq(1001L), eq(arguments), eq(false), eq("MCP 工具已停用: tool_save"), anyLong());
    }

    /**
     * Verifies rate limited tools are rejected and recorded without invoking the tool.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldRejectRateLimitedToolAndRecordFailure() {
        Fixture fixture = buildFixture();
        Map<String, Object> arguments = Map.of("title", "demo");
        when(fixture.registry.getTool("tool_save")).thenReturn(fixture.definition);
        when(fixture.configService.isToolEnabled("tool_save")).thenReturn(true);
        when(fixture.redisUtil.increment("mcp:tool:rate:1001:tool_save", 1)).thenReturn(31L);

        McpSchema.CallToolResult result = fixture.service.invoke("tool_save", arguments, 1001L, null);

        assertTrue(result.isError());
        verify(fixture.invoker, never()).invoke(any(), any(), any(), any());
        verify(fixture.logService).record(eq("tool_save"), eq(1001L), eq(arguments), eq(false), eq("工具调用过于频繁，请稍后再试"), anyLong());
    }

    private Fixture buildFixture() {
        McpToolRegistry registry = mock(McpToolRegistry.class);
        McpToolInvoker invoker = mock(McpToolInvoker.class);
        IMcpToolConfigService configService = mock(IMcpToolConfigService.class);
        IMcpToolCallLogService logService = mock(IMcpToolCallLogService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        McpToolCallServiceImpl service = new McpToolCallServiceImpl(
                registry,
                invoker,
                configService,
                logService,
                redisUtil
        );
        return new Fixture(service, registry, invoker, configService, logService, redisUtil, buildDefinition());
    }

    private McpToolDefinition buildDefinition() {
        McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of("title", Map.of("type", "string", "description", "标题")),
                List.of("title"),
                false,
                Map.of(),
                Map.of()
        );
        McpSchema.Tool schema = new McpSchema.Tool(
                "tool_save",
                "tool_save",
                "保存工具",
                inputSchema,
                null,
                new McpSchema.ToolAnnotations("tool_save", false, false, false, false, null),
                null
        );
        return new McpToolDefinition(
                "tool_save",
                "保存工具",
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

    private record Fixture(McpToolCallServiceImpl service,
                           McpToolRegistry registry,
                           McpToolInvoker invoker,
                           IMcpToolConfigService configService,
                           IMcpToolCallLogService logService,
                           RedisUtil redisUtil,
                           McpToolDefinition definition) {
    }
}
