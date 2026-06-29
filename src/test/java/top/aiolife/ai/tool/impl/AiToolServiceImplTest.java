package top.aiolife.ai.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import org.junit.jupiter.api.Test;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.ai.tool.AiMcpToolExecutor;
import top.aiolife.ai.tool.AiToolSchemaConverter;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.schema.McpFieldSchemaResolver;
import top.aiolife.mcp.service.IMcpToolCallService;
import top.aiolife.mcp.service.IMcpToolConfigService;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 工具适配服务单元测试。
 *
 * @author Ethan
 * @date 2026-06-29
 */
class AiToolServiceImplTest {

    /**
     * 验证空工具配置不会构建工具。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldReturnEmptyToolsWhenConfigMissingOrEmpty() {
        TestContext context = new TestContext();

        assertTrue(context.service.buildTools(1L, null).isEmpty());
        assertTrue(context.service.buildTools(1L, buildAgent("[]")).isEmpty());
        verify(context.mcpToolRegistry, never()).getTool(any());
    }

    /**
     * 验证无效 JSON 兜底为空工具列表。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldReturnEmptyToolsWhenJsonInvalid() {
        TestContext context = new TestContext();

        Map<ToolSpecification, ToolExecutor> result = context.service.buildTools(1L, buildAgent("{bad json"));

        assertTrue(result.isEmpty());
        verify(context.mcpToolRegistry, never()).getTool(any());
    }

    /**
     * 验证已注册且启用的 MCP 工具会被转换成 LangChain4j 工具。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldBuildRegisteredAndEnabledTool() throws NoSuchMethodException {
        TestContext context = new TestContext();
        McpToolDefinition definition = buildDefinition("thought_save");

        when(context.mcpToolRegistry.getTool("thought_save")).thenReturn(definition);
        when(context.mcpToolConfigService.isToolEnabled("thought_save")).thenReturn(true);
        when(context.mcpFieldSchemaResolver.buildSchema(eq(TestToolInput.class), eq(""), any())).thenReturn(buildSchema());

        Map<ToolSpecification, ToolExecutor> result = context.service.buildTools(1L, buildAgent("[\"thought_save\"]"));

        assertEquals(1, result.size());
        assertEquals("thought_save", result.keySet().iterator().next().name());
        assertInstanceOf(AiMcpToolExecutor.class, result.values().iterator().next());
    }

    /**
     * 验证未注册或已禁用工具会被过滤。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldSkipMissingOrDisabledTools() throws NoSuchMethodException {
        TestContext context = new TestContext();
        McpToolDefinition definition = buildDefinition("thought_save");

        when(context.mcpToolRegistry.getTool("missing_tool")).thenReturn(null);
        when(context.mcpToolRegistry.getTool("thought_save")).thenReturn(definition);
        when(context.mcpToolConfigService.isToolEnabled("thought_save")).thenReturn(false);

        Map<ToolSpecification, ToolExecutor> result = context.service.buildTools(1L, buildAgent("[\"missing_tool\",\"thought_save\"]"));

        assertTrue(result.isEmpty());
    }

    /**
     * 验证工具名会去重并过滤空白。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldDeduplicateAndTrimToolNames() throws NoSuchMethodException {
        TestContext context = new TestContext();
        McpToolDefinition definition = buildDefinition("thought_save");

        when(context.mcpToolRegistry.getTool("thought_save")).thenReturn(definition);
        when(context.mcpToolConfigService.isToolEnabled("thought_save")).thenReturn(true);
        when(context.mcpFieldSchemaResolver.buildSchema(eq(TestToolInput.class), eq(""), any())).thenReturn(buildSchema());

        Map<ToolSpecification, ToolExecutor> result = context.service.buildTools(1L, buildAgent("[\" thought_save \",\"\",\"thought_save\"]"));

        assertEquals(1, result.size());
        verify(context.mcpToolRegistry).getTool("thought_save");
    }

    private AiAgentConfigVO buildAgent(String enabledTools) {
        AiAgentConfigVO vo = new AiAgentConfigVO();
        vo.setEnabledTools(enabledTools);
        return vo;
    }

    private Map<String, Object> buildSchema() {
        return Map.of(
                "properties", Map.of(
                        "title", Map.of("type", "string", "description", "标题")
                ),
                "required", List.of("title"),
                "additionalProperties", false
        );
    }

    private McpToolDefinition buildDefinition(String name) throws NoSuchMethodException {
        Method method = TestToolBean.class.getDeclaredMethod("save", TestToolInput.class);
        McpOperation operation = method.getAnnotation(McpOperation.class);
        return new McpToolDefinition(
                name,
                "保存闪念",
                new TestToolBean(),
                method,
                TestToolInput.class,
                Object.class,
                null,
                true,
                true,
                null,
                operation
        );
    }

    private static class TestContext {

        private final McpToolRegistry mcpToolRegistry = mock(McpToolRegistry.class);

        private final IMcpToolConfigService mcpToolConfigService = mock(IMcpToolConfigService.class);

        private final IMcpToolCallService mcpToolCallService = mock(IMcpToolCallService.class);

        private final McpFieldSchemaResolver mcpFieldSchemaResolver = mock(McpFieldSchemaResolver.class);

        private final AiToolServiceImpl service = new AiToolServiceImpl(
                mcpToolRegistry,
                mcpToolConfigService,
                mcpToolCallService,
                mcpFieldSchemaResolver,
                new AiToolSchemaConverter(),
                new ObjectMapper()
        );
    }

    private static class TestToolBean {

        @McpOperation(name = "thought_save", description = "保存闪念")
        private Object save(TestToolInput input) {
            return input;
        }
    }

    private static class TestToolInput {

        private String title;
    }
}
