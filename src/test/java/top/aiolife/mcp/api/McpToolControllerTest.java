package top.aiolife.mcp.api;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.pojo.entity.McpToolCallLogEntity;
import top.aiolife.mcp.pojo.req.McpToolCallReq;
import top.aiolife.mcp.pojo.vo.McpToolVO;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.service.IMcpToolCallLogService;
import top.aiolife.mcp.service.IMcpToolCallService;
import top.aiolife.mcp.service.IMcpToolConfigService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MCP tool management controller test.
 *
 * @author Ethan
 * @date 2026-06-03
 */
class McpToolControllerTest {

    /**
     * Verifies the list API returns merged MCP tools from the configuration service.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldListMergedMcpToolsWithInputSchema() {
        McpToolRegistry registry = mock(McpToolRegistry.class);
        IMcpToolConfigService configService = mock(IMcpToolConfigService.class);
        IMcpToolCallService callService = mock(IMcpToolCallService.class);
        IMcpToolCallLogService logService = mock(IMcpToolCallLogService.class);
        McpToolDefinition definition = buildDefinition();
        McpToolVO vo = new McpToolVO();
        vo.setName("time_record_save");
        vo.setDisplayName("保存时间记录");
        vo.setDescription("保存时间记录");
        vo.setInputSchema(definition.schema().inputSchema());
        vo.setParamCount(2);
        vo.setGroupName("时间");
        vo.setEnabled(true);
        vo.setConfigured(false);
        vo.setRuntimeRegistered(true);
        vo.setWriteOperation(true);
        when(registry.getAllTools()).thenReturn(List.of(definition));
        when(configService.listMergedTools(List.of(definition))).thenReturn(List.of(vo));

        McpToolController controller = new McpToolController(registry, configService, callService, logService);
        ApiResponse<List<McpToolVO>> response = controller.listTools();

        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("time_record_save", response.getData().getFirst().getName());
        assertEquals(2, response.getData().getFirst().getParamCount());
        assertEquals(true, response.getData().getFirst().getWriteOperation());
    }

    /**
     * Verifies the call API delegates to the shared MCP tool call service.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldDelegateCallToSharedCallService() {
        McpToolRegistry registry = mock(McpToolRegistry.class);
        IMcpToolConfigService configService = mock(IMcpToolConfigService.class);
        IMcpToolCallService callService = mock(IMcpToolCallService.class);
        IMcpToolCallLogService logService = mock(IMcpToolCallLogService.class);
        Map<String, Object> arguments = Map.of("title", "test");
        McpSchema.CallToolResult callResult = McpSchema.CallToolResult.builder()
                .addTextContent("ok")
                .isError(false)
                .build();
        when(callService.invoke(eq("time_record_save"), eq(arguments), isNull(), isNull())).thenReturn(callResult);

        McpToolController controller = new McpToolController(registry, configService, callService, logService);
        McpToolCallReq req = new McpToolCallReq();
        req.setArguments(arguments);
        ApiResponse<McpSchema.CallToolResult> response = controller.callTool("time_record_save", req);

        assertNotNull(response.getData());
        assertFalse(response.getData().isError());
        assertEquals("ok", ((McpSchema.TextContent) response.getData().content().getFirst()).text());
    }

    /**
     * Verifies the audit log API returns recent logs from the log service.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldListRecentToolLogs() {
        McpToolRegistry registry = mock(McpToolRegistry.class);
        IMcpToolConfigService configService = mock(IMcpToolConfigService.class);
        IMcpToolCallService callService = mock(IMcpToolCallService.class);
        IMcpToolCallLogService logService = mock(IMcpToolCallLogService.class);
        McpToolCallLogEntity log = new McpToolCallLogEntity();
        log.setToolName("time_record_save");
        log.setSuccess(true);
        when(logService.listRecentLogs("time_record_save", 50)).thenReturn(List.of(log));

        McpToolController controller = new McpToolController(registry, configService, callService, logService);
        ApiResponse<List<McpToolCallLogEntity>> response = controller.listLogs("time_record_save", 50);

        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("time_record_save", response.getData().getFirst().getToolName());
    }

    private McpToolDefinition buildDefinition() {
        McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of(
                        "title", Map.of("type", "string", "description", "标题"),
                        "date", Map.of("type", "string", "description", "日期")
                ),
                List.of("title"),
                false,
                Map.of(),
                Map.of()
        );
        McpSchema.Tool schema = new McpSchema.Tool(
                "time_record_save",
                "time_record_save",
                "保存时间记录",
                inputSchema,
                null,
                new McpSchema.ToolAnnotations("time_record_save", false, false, false, false, null),
                null
        );
        return new McpToolDefinition(
                "time_record_save",
                "保存时间记录",
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
