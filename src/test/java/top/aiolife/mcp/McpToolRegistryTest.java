package top.aiolife.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import top.aiolife.mcp.adapter.LangChain4jToolSchemaAdapter;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.schema.McpFieldSchemaResolver;
import top.aiolife.mcp.schema.McpSchemaGenerator;
import top.aiolife.mcp.tools.FoodRecordMcpTools;
import top.aiolife.mcp.tools.ThoughtMcpTools;
import top.aiolife.mcp.tools.TimeRecordMcpTools;
import top.aiolife.record.service.IThoughtService;
import top.aiolife.record.service.FoodRecordAiFacade;
import top.aiolife.record.service.TimeRecordAiFacade;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MCP 工具注册器测试。
 *
 * @author Ethan
 * @date 2026-05-31
 */
class McpToolRegistryTest {

    @Test
    void shouldRegisterAllMcpToolsWithoutDuplicateNames() {
        McpToolRegistry registry = createRegistry();

        Collection<McpToolDefinition> tools = registry.getAllTools();
        Set<String> names = new HashSet<>();
        tools.forEach(tool -> names.add(tool.name()));

        assertEquals(5, tools.size());
        assertEquals(5, names.size());
        assertNotNull(registry.getTool("thought_save"));
        assertTrue(names.contains("time_record_save"));
        assertTrue(names.contains("time_record_queryByDateRange"));
        assertTrue(names.contains("food_record_save"));
        assertTrue(names.contains("food_record_query"));
    }

    @Test
    void shouldExposeSchemaFromRegisteredDefinition() {
        McpToolRegistry registry = createRegistry();
        McpSchema.Tool tool = registry.getTool("thought_save").schema();

        assertEquals("thought_save", tool.name());
        assertTrue(tool.inputSchema().properties().containsKey("subject"));
        assertTrue(tool.inputSchema().properties().containsKey("content"));
    }

    private McpToolRegistry createRegistry() {
        ThoughtMcpTools thoughtTools = new ThoughtMcpTools(mock(IThoughtService.class));
        TimeRecordMcpTools timeRecordTools = new TimeRecordMcpTools(mock(TimeRecordAiFacade.class));
        FoodRecordMcpTools foodRecordTools = new FoodRecordMcpTools(mock(FoodRecordAiFacade.class));
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"thoughtMcpTools", "timeRecordMcpTools", "foodRecordMcpTools"});
        doReturn(ThoughtMcpTools.class).when(applicationContext).getType("thoughtMcpTools");
        doReturn(TimeRecordMcpTools.class).when(applicationContext).getType("timeRecordMcpTools");
        doReturn(FoodRecordMcpTools.class).when(applicationContext).getType("foodRecordMcpTools");
        when(applicationContext.getBean("thoughtMcpTools")).thenReturn(thoughtTools);
        when(applicationContext.getBean("timeRecordMcpTools")).thenReturn(timeRecordTools);
        when(applicationContext.getBean("foodRecordMcpTools")).thenReturn(foodRecordTools);

        McpFieldSchemaResolver resolver = new McpFieldSchemaResolver();
        McpSchemaGenerator generator = new McpSchemaGenerator(resolver);
        McpToolRegistry registry = new McpToolRegistry(applicationContext, new LangChain4jToolSchemaAdapter(generator));
        registry.init();
        return registry;
    }
}
