package top.aiolife.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import top.aiolife.mcp.adapter.LangChain4jToolSchemaAdapter;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.schema.McpFieldSchemaResolver;
import top.aiolife.mcp.schema.McpSchemaGenerator;
import top.aiolife.mcp.tools.FoodRecordMcpTools;
import top.aiolife.mcp.tools.ProblemNoteMcpTools;
import top.aiolife.mcp.tools.ThoughtMcpTools;
import top.aiolife.mcp.tools.TimeRecordMcpTools;
import top.aiolife.record.mapper.IRelaEventMapper;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.service.IThoughtService;
import top.aiolife.record.service.FoodRecordAiFacade;
import top.aiolife.record.service.ProblemNoteAiFacade;
import top.aiolife.record.service.TimeRecordAiFacade;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MCP 工具兼容性测试。
 *
 * @author Ethan
 * @date 2026-06-10
 */
class McpToolCompatibilityTest {

    @Test
    void shouldKeepPublicToolNamesCompatible() {
        McpToolRegistry registry = createRegistry();
        Set<String> toolNames = registry.getAllTools().stream()
                .map(tool -> tool.name())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "thought_save",
                "thought_query",
                "time_record_save",
                "time_record_queryByDateRange",
                "food_record_save",
                "food_record_query",
                "problem_note_query",
                "problem_note_save"
        ), toolNames);
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

    @Test
    void shouldExposeFoodRecordInputFields() {
        McpToolRegistry registry = createRegistry();

        Map<String, Object> saveProperties = registry.getTool("food_record_save").schema().inputSchema().properties();
        assertTrue(saveProperties.containsKey("id"));
        assertTrue(saveProperties.containsKey("idempotencyKey"));
        assertTrue(saveProperties.containsKey("dishName"));
        assertTrue(saveProperties.containsKey("cookDate"));
        assertTrue(saveProperties.containsKey("status"));
        assertTrue(saveProperties.containsKey("ingredients"));
        assertTrue(saveProperties.containsKey("steps"));
        assertTrue(saveProperties.containsKey("worthRedo"));
        assertFalse(saveProperties.containsKey("difficulty"));
        assertFalse(saveProperties.containsKey("successLevel"));
        assertFalse(saveProperties.containsKey("prepMinutes"));
        assertFalse(saveProperties.containsKey("cookMinutes"));
        assertFalse(saveProperties.containsKey("totalMinutes"));
        assertFalse(saveProperties.containsKey("tasteDescription"));
        assertFalse(saveProperties.containsKey("briefSummary"));
        assertFalse(saveProperties.containsKey("nextTrySuggestion"));

        Map<String, Object> queryProperties = registry.getTool("food_record_query").schema().inputSchema().properties();
        assertTrue(queryProperties.containsKey("keyword"));
        assertTrue(queryProperties.containsKey("category"));
        assertTrue(queryProperties.containsKey("mealType"));
        assertTrue(queryProperties.containsKey("rating"));
        assertTrue(queryProperties.containsKey("worthRedo"));
        assertTrue(queryProperties.containsKey("toImprove"));
    }

    @Test
    void shouldExposeProblemNoteInputFields() {
        McpToolRegistry registry = createRegistry();

        Map<String, Object> queryProperties = registry.getTool("problem_note_query").schema().inputSchema().properties();
        assertTrue(queryProperties.containsKey("page"));
        assertTrue(queryProperties.containsKey("pageSize"));
        assertTrue(queryProperties.containsKey("keyword"));
        assertTrue(queryProperties.containsKey("difficulty"));
        assertTrue(queryProperties.containsKey("status"));
        assertTrue(queryProperties.containsKey("tags"));
        assertTrue(queryProperties.containsKey("categoryId"));
        assertTrue(queryProperties.containsKey("uncategorized"));

        Map<String, Object> saveProperties = registry.getTool("problem_note_save").schema().inputSchema().properties();
        assertTrue(saveProperties.containsKey("idempotencyKey"));
        assertTrue(saveProperties.containsKey("categoryId"));
        assertTrue(saveProperties.containsKey("title"));
        assertTrue(saveProperties.containsKey("problemContent"));
        assertTrue(saveProperties.containsKey("solutionCode"));
        assertTrue(saveProperties.containsKey("pseudoCode"));
        assertTrue(saveProperties.containsKey("ideaNote"));
        assertTrue(saveProperties.containsKey("difficulty"));
        assertTrue(saveProperties.containsKey("tags"));
        assertTrue(saveProperties.containsKey("status"));
        assertFalse(saveProperties.containsKey("id"));
    }

    private McpToolRegistry createRegistry() {
        ThoughtMcpTools thoughtTools = new ThoughtMcpTools(
                mock(IThoughtService.class),
                mock(IThoughtMapper.class),
                mock(IRelaEventMapper.class));
        TimeRecordMcpTools timeRecordTools = new TimeRecordMcpTools(mock(TimeRecordAiFacade.class));
        FoodRecordMcpTools foodRecordTools = new FoodRecordMcpTools(mock(FoodRecordAiFacade.class));
        ProblemNoteMcpTools problemNoteTools = new ProblemNoteMcpTools(mock(ProblemNoteAiFacade.class));
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"thoughtMcpTools", "timeRecordMcpTools", "foodRecordMcpTools", "problemNoteMcpTools"});
        doReturn(ThoughtMcpTools.class).when(applicationContext).getType("thoughtMcpTools");
        doReturn(TimeRecordMcpTools.class).when(applicationContext).getType("timeRecordMcpTools");
        doReturn(FoodRecordMcpTools.class).when(applicationContext).getType("foodRecordMcpTools");
        doReturn(ProblemNoteMcpTools.class).when(applicationContext).getType("problemNoteMcpTools");
        when(applicationContext.getBean("thoughtMcpTools")).thenReturn(thoughtTools);
        when(applicationContext.getBean("timeRecordMcpTools")).thenReturn(timeRecordTools);
        when(applicationContext.getBean("foodRecordMcpTools")).thenReturn(foodRecordTools);
        when(applicationContext.getBean("problemNoteMcpTools")).thenReturn(problemNoteTools);

        McpFieldSchemaResolver resolver = new McpFieldSchemaResolver();
        McpSchemaGenerator generator = new McpSchemaGenerator(resolver);
        McpToolRegistry registry = new McpToolRegistry(applicationContext, new LangChain4jToolSchemaAdapter(generator));
        registry.init();
        return registry;
    }
}
