package top.aiolife.mcp.definition;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import top.aiolife.mcp.annotation.McpOperation;

import java.lang.reflect.Method;

/**
 * MCP 工具定义。
 *
 * <p>用途：作为项目内部统一的 MCP 工具描述模型，承接注册、Schema 暴露与工具调用。</p>
 *
 * @author Ethan
 * @date 2026-05-31
 */
public record McpToolDefinition(
        String name,
        String description,
        Object bean,
        Method method,
        Class<?> inputType,
        Class<?> outputType,
        McpSchema.Tool schema,
        boolean authRequired,
        boolean unwrapApiResponse,
        ToolSpecification toolSpecification,
        McpOperation operation) {
}
