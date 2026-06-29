package top.aiolife.ai.tool.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.ai.tool.AiMcpToolExecutor;
import top.aiolife.ai.tool.AiToolSchemaConverter;
import top.aiolife.ai.tool.AiToolService;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.schema.McpFieldSchemaResolver;
import top.aiolife.mcp.service.IMcpToolCallService;
import top.aiolife.mcp.service.IMcpToolConfigService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI 工具适配服务实现。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Service
@RequiredArgsConstructor
public class AiToolServiceImpl implements AiToolService {

    private final McpToolRegistry mcpToolRegistry;

    private final IMcpToolConfigService mcpToolConfigService;

    private final IMcpToolCallService mcpToolCallService;

    private final McpFieldSchemaResolver mcpFieldSchemaResolver;

    private final AiToolSchemaConverter aiToolSchemaConverter;

    private final ObjectMapper objectMapper;

    /**
     * 根据 Agent 配置构建当前用户可用的 LangChain4j 工具映射。
     *
     * @param userId 当前登录用户 id
     * @param agentConfig Agent 生效配置
     * @return LangChain4j 工具定义和执行器映射
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public Map<ToolSpecification, ToolExecutor> buildTools(Long userId, AiAgentConfigVO agentConfig) {
        Set<String> enabledToolNames = parseEnabledTools(agentConfig == null ? null : agentConfig.getEnabledTools());
        if (enabledToolNames.isEmpty()) {
            return Map.of();
        }
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Map<ToolSpecification, ToolExecutor> tools = new LinkedHashMap<>();
        for (String toolName : enabledToolNames) {
            McpToolDefinition definition = mcpToolRegistry.getTool(toolName);
            if (definition == null || !mcpToolConfigService.isToolEnabled(toolName)) {
                continue;
            }
            tools.put(
                    toToolSpecification(definition),
                    new AiMcpToolExecutor(toolName, userId, requestAttributes, mcpToolCallService, objectMapper)
            );
        }
        return tools;
    }

    private Set<String> parseEnabledTools(String enabledTools) {
        if (!StringUtils.hasText(enabledTools)) {
            return Set.of();
        }
        try {
            List<String> values = objectMapper.readValue(enabledTools, new TypeReference<>() {
            });
            return values.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        } catch (Exception ignored) {
            return Set.of();
        }
    }

    private ToolSpecification toToolSpecification(McpToolDefinition definition) {
        Map<String, Object> schemaMap = mcpFieldSchemaResolver.buildSchema(
                definition.inputType(),
                "",
                Set.of(definition.operation().ignoreInputFields())
        );
        return ToolSpecification.builder()
                .name(definition.name())
                .description(definition.description())
                .parameters(aiToolSchemaConverter.toObjectSchema(schemaMap))
                .build();
    }
}
