package top.aiolife.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import top.aiolife.mcp.adapter.LangChain4jToolSchemaAdapter;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.schema.McpFieldSchemaResolver;
import top.aiolife.mcp.schema.McpSchemaGenerator;
import top.aiolife.mcp.tools.ThoughtMcpTools;
import top.aiolife.mcp.tools.TimeRecordMcpTools;
import top.aiolife.record.service.IThoughtService;
import top.aiolife.record.service.TimeRecordAiFacade;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MCP 工具兼容性测试。
 *
 * @author Ethan
 * @date 2026-05-31
 */
class McpToolCompatibilityTest {

    @Test
    void shouldKeepPublicToolNamesCompatible() {
        McpToolRegistry registry = createRegistry();
        Set<String> toolNames = registry.getAllTools().stream()
                .map(tool -> tool.name())
                .collect(Collectors.toSet());

        assertEquals(Set.of("thought_save", "time_record_save", "time_record_queryByDateRange"), toolNames);
    }

    @Test
    void shouldKeepThoughtSaveInputFieldsCompatible() {
        McpSchema.Tool tool = createRegistry().getTool("thought_save").schema();
        Map<String, Object> properties = tool.inputSchema().properties();

        assertTrue(properties.containsKey("subject"));
        assertTrue(properties.containsKey("content"));
        assertTrue(properties.containsKey("themeKey"));
        assertTrue(properties.containsKey("status"));
        assertTrue(properties.containsKey("events"));
        assertTrue(properties.containsKey("idempotencyKey"));
    }

    @Test
    void shouldKeepTimeRecordInputFieldsCompatible() {
        McpToolRegistry registry = createRegistry();

        Map<String, Object> queryProperties = registry.getTool("time_record_queryByDateRange").schema().inputSchema().properties();
        assertTrue(queryProperties.containsKey("startDate"));
        assertTrue(queryProperties.containsKey("endDate"));

        Map<String, Object> saveProperties = registry.getTool("time_record_save").schema().inputSchema().properties();
        assertTrue(saveProperties.containsKey("categoryId"));
        assertTrue(saveProperties.containsKey("date"));
        assertTrue(saveProperties.containsKey("startTime"));
        assertTrue(saveProperties.containsKey("endTime"));
        assertTrue(saveProperties.containsKey("title"));
        assertTrue(saveProperties.containsKey("description"));
        assertTrue(saveProperties.containsKey("exercises"));
        assertTrue(saveProperties.containsKey("idempotencyKey"));
    }

    private McpToolRegistry createRegistry() {
        ThoughtMcpTools thoughtTools = new ThoughtMcpTools(mock(IThoughtService.class));
        TimeRecordMcpTools timeRecordTools = new TimeRecordMcpTools(mock(TimeRecordAiFacade.class));
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"thoughtMcpTools", "timeRecordMcpTools"});
        doReturn(ThoughtMcpTools.class).when(applicationContext).getType("thoughtMcpTools");
        doReturn(TimeRecordMcpTools.class).when(applicationContext).getType("timeRecordMcpTools");
        when(applicationContext.getBean("thoughtMcpTools")).thenReturn(thoughtTools);
        when(applicationContext.getBean("timeRecordMcpTools")).thenReturn(timeRecordTools);

        McpFieldSchemaResolver resolver = new McpFieldSchemaResolver();
        McpSchemaGenerator generator = new McpSchemaGenerator(resolver);
        McpToolRegistry registry = new McpToolRegistry(applicationContext, new LangChain4jToolSchemaAdapter(generator));
        registry.init();
        return registry;
    }
}
