package top.aiolife.mcp.registry;

import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.mcp.adapter.LangChain4jToolSchemaAdapter;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.definition.McpToolDefinition;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MCP 工具注册表。
 *
 * <p>用途：扫描 MCP 工具组件并注册工具定义，供 MCP Server 暴露 tools/list 与 tools/call。</p>
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolRegistry {

    private static final String MCP_TOOL_PACKAGE_PREFIX = "top.aiolife.mcp.tools";

    private final ApplicationContext applicationContext;
    private final LangChain4jToolSchemaAdapter schemaAdapter;

    private final Map<String, McpToolDefinition> registeredTools = new LinkedHashMap<>();

    /**
     * 初始化 MCP 工具注册表。
     *
     * <p>用途：扫描 MCP 工具包下 Spring Bean 中标注 {@link McpOperation} 的单入参方法。</p>
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @PostConstruct
    public void init() {
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> type = applicationContext.getType(beanName);
            if (type == null || type.getPackage() == null) {
                continue;
            }
            if (!type.getPackage().getName().startsWith(MCP_TOOL_PACKAGE_PREFIX)) {
                continue;
            }
            registerToolMethods(applicationContext.getBean(beanName));
        }
        log.info("MCP 工具注册完成，共 {} 个", registeredTools.size());
    }

    /**
     * 获取全部已注册 MCP 工具。
     *
     * @return 已注册 MCP 工具集合
     *
     * @author Ethan
     * @date 2026-05-31
     */
    public Collection<McpToolDefinition> getAllTools() {
        return registeredTools.values();
    }

    /**
     * 按工具名称获取已注册 MCP 工具。
     *
     * @param name 工具名称
     * @return 已注册 MCP 工具；不存在时返回 null
     *
     * @author Ethan
     * @date 2026-05-31
     */
    public McpToolDefinition getTool(String name) {
        return registeredTools.get(name);
    }

    private void registerToolMethods(Object bean) {
        Class<?> targetClass = AopUtils.getTargetClass(Objects.requireNonNull(bean));
        for (Method method : targetClass.getDeclaredMethods()) {
            McpOperation mcpOperation = method.getAnnotation(McpOperation.class);
            if (mcpOperation == null) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                throw new IllegalStateException("MCP 工具目前仅支持单个入参方法: " + method);
            }
            if (!StringUtils.hasText(mcpOperation.name())) {
                throw new IllegalStateException("MCP 工具名称不能为空: " + method);
            }
            String toolName = mcpOperation.name();
            String description = StringUtils.hasText(mcpOperation.description()) ? mcpOperation.description() : toolName;
            McpSchema.Tool mcpTool = schemaAdapter.toMcpTool(toolName, description, method, null, mcpOperation);
            McpToolDefinition toolDefinition = new McpToolDefinition(
                    toolName,
                    description,
                    bean,
                    method,
                    method.getParameterTypes()[0],
                    method.getReturnType(),
                    mcpTool,
                    true,
                    mcpOperation.unwrapApiResponseData(),
                    null,
                    mcpOperation
            );
            McpToolDefinition existing = registeredTools.putIfAbsent(toolName, toolDefinition);
            if (existing != null) {
                throw new IllegalStateException("MCP 工具名称重复: " + toolName);
            }
            log.info("注册 MCP 工具: {} -> {}.{}", toolName, targetClass.getSimpleName(), method.getName());
        }
    }
}
