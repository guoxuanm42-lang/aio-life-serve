package top.aiolife.mcp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.mapper.IMcpToolConfigMapper;
import top.aiolife.mcp.pojo.entity.McpToolConfigEntity;
import top.aiolife.mcp.pojo.req.McpToolConfigSaveReq;
import top.aiolife.mcp.pojo.vo.McpToolVO;
import top.aiolife.mcp.service.IMcpToolConfigService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool configuration service implementation.
 *
 * <p>Maintains global MCP tool operation configuration and merges it with runtime registry definitions.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Service
@RequiredArgsConstructor
public class McpToolConfigServiceImpl extends ServiceImpl<IMcpToolConfigMapper, McpToolConfigEntity>
        implements IMcpToolConfigService {

    private static final String DESCRIPTION_SOURCE_NONE = "none";
    private static final String DESCRIPTION_SOURCE_OVERRIDE = "override";
    private static final String DESCRIPTION_SOURCE_RUNTIME = "runtime";

    private final IMcpToolConfigMapper mcpToolConfigMapper;

    /**
     * Lists MCP tools merged from runtime registry and database configuration.
     *
     * @param definitions runtime MCP tool definitions discovered by registry
     * @return merged MCP tool view objects ordered by sort order and tool name
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Override
    public List<McpToolVO> listMergedTools(Collection<McpToolDefinition> definitions) {
        Map<String, McpToolDefinition> definitionMap = new LinkedHashMap<>();
        if (definitions != null) {
            for (McpToolDefinition definition : definitions) {
                if (definition != null && StringUtils.hasText(definition.name())) {
                    definitionMap.put(definition.name(), definition);
                }
            }
        }

        Map<String, McpToolConfigEntity> configMap = new LinkedHashMap<>();
        for (McpToolConfigEntity config : listAllConfigs()) {
            if (config != null && StringUtils.hasText(config.getToolName())) {
                configMap.put(config.getToolName(), config);
            }
        }

        Map<String, String> toolNames = new LinkedHashMap<>();
        definitionMap.keySet().forEach(name -> toolNames.put(name, name));
        configMap.keySet().forEach(name -> toolNames.put(name, name));

        List<McpToolVO> result = new ArrayList<>();
        for (String toolName : toolNames.keySet()) {
            result.add(toVO(toolName, definitionMap.get(toolName), configMap.get(toolName)));
        }
        result.sort(Comparator
                .comparing((McpToolVO vo) -> vo.getSortOrder() == null ? 0 : vo.getSortOrder())
                .thenComparing(McpToolVO::getName, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    /**
     * Saves editable configuration for one MCP tool.
     *
     * @param toolName tool name from path variable
     * @param req configuration request body
     * @return saved configuration entity
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Override
    public McpToolConfigEntity saveConfig(String toolName, McpToolConfigSaveReq req) {
        if (!StringUtils.hasText(toolName)) {
            throw new IllegalArgumentException("MCP 工具名称不能为空");
        }
        McpToolConfigEntity entity = getByToolName(toolName);
        LocalDateTime now = LocalDateTime.now();
        if (entity == null) {
            entity = new McpToolConfigEntity();
            entity.setToolName(toolName.trim());
            entity.setEnabled(true);
            entity.setCreateTime(now);
        }
        McpToolConfigSaveReq request = req == null ? new McpToolConfigSaveReq() : req;
        entity.setDisplayName(normalizeBlank(request.getDisplayName()));
        entity.setGroupName(normalizeBlank(request.getGroupName()));
        entity.setDescriptionOverride(normalizeBlank(request.getDescriptionOverride()));
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entity.setWriteOperation(Boolean.TRUE.equals(request.getWriteOperation()));
        entity.setRemark(normalizeBlank(request.getRemark()));
        entity.setUpdateTime(now);
        if (entity.getId() == null) {
            mcpToolConfigMapper.insert(entity);
        } else {
            mcpToolConfigMapper.updateById(entity);
        }
        return entity;
    }

    /**
     * Updates enablement status for one MCP tool.
     *
     * @param toolName tool name from path variable
     * @param enabled whether the tool should be enabled
     * @return saved configuration entity
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Override
    public McpToolConfigEntity updateStatus(String toolName, Boolean enabled) {
        if (!StringUtils.hasText(toolName)) {
            throw new IllegalArgumentException("MCP 工具名称不能为空");
        }
        if (enabled == null) {
            throw new IllegalArgumentException("enabled 不能为空");
        }
        McpToolConfigEntity entity = getByToolName(toolName);
        LocalDateTime now = LocalDateTime.now();
        if (entity == null) {
            entity = new McpToolConfigEntity();
            entity.setToolName(toolName.trim());
            entity.setSortOrder(0);
            entity.setCreateTime(now);
        }
        entity.setEnabled(enabled);
        entity.setUpdateTime(now);
        if (entity.getId() == null) {
            mcpToolConfigMapper.insert(entity);
        } else {
            mcpToolConfigMapper.updateById(entity);
        }
        return entity;
    }

    /**
     * Checks whether a tool is enabled by operation configuration.
     *
     * @param toolName runtime tool name
     * @return true when no config exists or config enabled is not false
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Override
    public boolean isToolEnabled(String toolName) {
        if (!StringUtils.hasText(toolName)) {
            return false;
        }
        McpToolConfigEntity config = getByToolName(toolName);
        return config == null || !Boolean.FALSE.equals(config.getEnabled());
    }

    private List<McpToolConfigEntity> listAllConfigs() {
        LambdaQueryWrapper<McpToolConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(McpToolConfigEntity::getGroupName);
        wrapper.orderByAsc(McpToolConfigEntity::getSortOrder);
        wrapper.orderByAsc(McpToolConfigEntity::getToolName);
        return mcpToolConfigMapper.selectList(wrapper);
    }

    private McpToolConfigEntity getByToolName(String toolName) {
        LambdaQueryWrapper<McpToolConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(McpToolConfigEntity::getToolName, toolName.trim());
        wrapper.last("limit 1");
        return mcpToolConfigMapper.selectOne(wrapper);
    }

    private McpToolVO toVO(String toolName, McpToolDefinition definition, McpToolConfigEntity config) {
        McpToolVO vo = new McpToolVO();
        vo.setName(toolName);
        vo.setDisplayName(resolveDisplayName(toolName, config));
        vo.setDescription(resolveDescription(definition, config));
        vo.setDescriptionSource(resolveDescriptionSource(definition, config));
        vo.setInputSchema(definition == null ? null : definition.schema().inputSchema());
        vo.setParamCount(definition == null ? 0 : countParams(definition.schema().inputSchema()));
        vo.setAuthRequired(definition != null && definition.authRequired());
        vo.setEnabled(config == null || !Boolean.FALSE.equals(config.getEnabled()));
        vo.setWriteOperation(config != null && Boolean.TRUE.equals(config.getWriteOperation()));
        vo.setGroupName(resolveGroupName(toolName, config));
        vo.setConfigured(config != null);
        vo.setRuntimeRegistered(definition != null);
        vo.setRemark(config == null ? null : config.getRemark());
        vo.setSortOrder(config == null || config.getSortOrder() == null ? 0 : config.getSortOrder());
        return vo;
    }

    private String resolveDisplayName(String toolName, McpToolConfigEntity config) {
        if (config != null && StringUtils.hasText(config.getDisplayName())) {
            return config.getDisplayName();
        }
        return toolName;
    }

    private String resolveDescription(McpToolDefinition definition, McpToolConfigEntity config) {
        if (config != null && StringUtils.hasText(config.getDescriptionOverride())) {
            return config.getDescriptionOverride();
        }
        return definition == null ? null : definition.description();
    }

    private String resolveDescriptionSource(McpToolDefinition definition, McpToolConfigEntity config) {
        if (config != null && StringUtils.hasText(config.getDescriptionOverride())) {
            return DESCRIPTION_SOURCE_OVERRIDE;
        }
        if (definition != null && StringUtils.hasText(definition.description())) {
            return DESCRIPTION_SOURCE_RUNTIME;
        }
        return DESCRIPTION_SOURCE_NONE;
    }

    private Integer countParams(McpSchema.JsonSchema inputSchema) {
        if (inputSchema == null) {
            return 0;
        }
        Map<String, Object> properties = inputSchema.properties();
        return properties == null ? 0 : properties.size();
    }

    private String resolveGroupName(String toolName, McpToolConfigEntity config) {
        if (config != null && StringUtils.hasText(config.getGroupName())) {
            return config.getGroupName();
        }
        if (toolName == null) {
            return "其他";
        }
        if (toolName.startsWith("time_record_") || toolName.startsWith("time_tracker_")) {
            return "时间";
        }
        if (toolName.startsWith("thought_")) {
            return "闪念";
        }
        if (toolName.startsWith("task_")) {
            return "任务";
        }
        if (toolName.startsWith("food_record_")) {
            return "美食";
        }
        if (toolName.startsWith("exercise_")) {
            return "运动";
        }
        return "其他";
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
