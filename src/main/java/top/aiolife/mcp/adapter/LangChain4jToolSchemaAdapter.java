package top.aiolife.mcp.adapter;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.schema.McpSchemaGenerator;

import java.lang.reflect.Method;

/**
 * LangChain4j 工具 Schema 兼容适配器。
 *
 * <p>用途：保留现有注册器入口，内部委托独立的 MCP Schema 生成器完成 Tool Schema 构建。</p>
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Component
@RequiredArgsConstructor
public class LangChain4jToolSchemaAdapter {

    private final McpSchemaGenerator schemaGenerator;

    /**
     * 将当前 LangChain4j 工具元数据转换为 MCP Tool Schema。
     *
     * @param name 工具名称
     * @param description 工具说明
     * @param method 工具方法
     * @param toolSpecification LangChain4j 工具说明
     * @param operation MCP 工具暴露配置
     * @return MCP Tool Schema
     *
     * @author Ethan
     * @date 2026-05-31
     */
    public McpSchema.Tool toMcpTool(String name,
                                    String description,
                                    Method method,
                                    ToolSpecification toolSpecification,
                                    McpOperation operation) {
        return schemaGenerator.generateTool(name, description, method, toolSpecification, operation);
    }
}
