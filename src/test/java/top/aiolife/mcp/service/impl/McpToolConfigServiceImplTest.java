package top.aiolife.mcp.service.impl;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.mapper.IMcpToolConfigMapper;
import top.aiolife.mcp.pojo.entity.McpToolConfigEntity;
import top.aiolife.mcp.pojo.vo.McpToolVO;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MCP tool configuration service implementation test.
 *
 * @author Ethan
 * @date 2026-06-03
 */
class McpToolConfigServiceImplTest {

    /**
     * Verifies runtime tools without database config are returned as unconfigured and enabled.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldReturnRuntimeToolWhenConfigMissing() {
        IMcpToolConfigMapper mapper = mock(IMcpToolConfigMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        McpToolConfigServiceImpl service = new McpToolConfigServiceImpl(mapper);

        List<McpToolVO> tools = service.listMergedTools(List.of(buildDefinition("task_list", "查询任务")));

        assertEquals(1, tools.size());
        McpToolVO tool = tools.getFirst();
        assertEquals("task_list", tool.getName());
        assertEquals("task_list", tool.getDisplayName());
        assertEquals("查询任务", tool.getDescription());
        assertEquals("任务", tool.getGroupName());
        assertFalse(tool.getConfigured());
        assertTrue(tool.getRuntimeRegistered());
        assertTrue(tool.getEnabled());
        assertEquals(2, tool.getParamCount());
    }

    /**
     * Verifies database config overrides display fields and ordering.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldApplyConfigOverrideAndSortOrder() {
        IMcpToolConfigMapper mapper = mock(IMcpToolConfigMapper.class);
        McpToolConfigEntity first = buildConfig("task_list", "任务列表", "研发", "覆盖说明", true, 10);
        McpToolConfigEntity second = buildConfig("thought_save", "保存闪念", "灵感", null, true, 1);
        when(mapper.selectList(any())).thenReturn(List.of(first, second));
        McpToolConfigServiceImpl service = new McpToolConfigServiceImpl(mapper);

        List<McpToolVO> tools = service.listMergedTools(List.of(
                buildDefinition("task_list", "运行时任务说明"),
                buildDefinition("thought_save", "运行时闪念说明")
        ));

        assertEquals("thought_save", tools.get(0).getName());
        assertEquals("task_list", tools.get(1).getName());
        McpToolVO configured = tools.get(1);
        assertEquals("任务列表", configured.getDisplayName());
        assertEquals("覆盖说明", configured.getDescription());
        assertEquals("override", configured.getDescriptionSource());
        assertEquals("研发", configured.getGroupName());
        assertTrue(configured.getConfigured());
    }

    /**
     * Verifies config-only tools remain visible but are marked as not registered.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldReturnConfigOnlyToolAsNotRegistered() {
        IMcpToolConfigMapper mapper = mock(IMcpToolConfigMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(
                buildConfig("offline_tool", "离线工具", "其他", "离线说明", true, 0)
        ));
        McpToolConfigServiceImpl service = new McpToolConfigServiceImpl(mapper);

        List<McpToolVO> tools = service.listMergedTools(List.of());

        assertEquals(1, tools.size());
        McpToolVO tool = tools.getFirst();
        assertEquals("offline_tool", tool.getName());
        assertEquals("离线工具", tool.getDisplayName());
        assertEquals("离线说明", tool.getDescription());
        assertTrue(tool.getConfigured());
        assertFalse(tool.getRuntimeRegistered());
        assertNull(tool.getInputSchema());
        assertEquals(0, tool.getParamCount());
    }

    /**
     * Verifies disabled config marks the merged tool as disabled.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldMarkDisabledConfigAsDisabled() {
        IMcpToolConfigMapper mapper = mock(IMcpToolConfigMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(
                buildConfig("task_list", "任务列表", "任务", null, false, 0)
        ));
        McpToolConfigServiceImpl service = new McpToolConfigServiceImpl(mapper);

        List<McpToolVO> tools = service.listMergedTools(List.of(buildDefinition("task_list", "查询任务")));

        assertFalse(tools.getFirst().getEnabled());
    }

    private McpToolConfigEntity buildConfig(String toolName,
                                            String displayName,
                                            String groupName,
                                            String descriptionOverride,
                                            Boolean enabled,
                                            Integer sortOrder) {
        McpToolConfigEntity entity = new McpToolConfigEntity();
        entity.setId((long) toolName.hashCode());
        entity.setToolName(toolName);
        entity.setDisplayName(displayName);
        entity.setGroupName(groupName);
        entity.setDescriptionOverride(descriptionOverride);
        entity.setEnabled(enabled);
        entity.setSortOrder(sortOrder);
        return entity;
    }

    private McpToolDefinition buildDefinition(String name, String description) {
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
                name,
                name,
                description,
                inputSchema,
                null,
                new McpSchema.ToolAnnotations(name, false, false, false, false, null),
                null
        );
        return new McpToolDefinition(
                name,
                description,
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
