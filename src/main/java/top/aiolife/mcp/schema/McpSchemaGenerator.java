package top.aiolife.mcp.schema;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.aiolife.mcp.annotation.McpOperation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP Schema 生成器。
 *
 * <p>用途：根据 MCP 工具方法和入参 DTO 生成 MCP Tool 与输入 JSON Schema。</p>
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Component
@RequiredArgsConstructor
public class McpSchemaGenerator {

    private final McpFieldSchemaResolver fieldSchemaResolver;

    /**
     * 生成 MCP 工具定义使用的 Schema。
     *
     * @param name 工具名称
     * @param description 工具说明
     * @param method 工具方法
     * @param toolSpecification LangChain4j 工具说明，当前阶段作为兼容输入；可为空
     * @param operation MCP 工具暴露配置
     * @return MCP Tool Schema
     *
     * @author Ethan
     * @date 2026-05-31
     */
    public McpSchema.Tool generateTool(String name,
                                       String description,
                                       Method method,
                                       ToolSpecification toolSpecification,
                                       McpOperation operation) {
        Set<String> ignoreFields = Set.of(operation.ignoreInputFields());
        Map<String, Object> schemaMap = fieldSchemaResolver.buildSchema(method.getGenericParameterTypes()[0], "", ignoreFields);
        McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                stringValue(schemaMap.get("type")),
                mapValue(schemaMap.get("properties")),
                listValue(schemaMap.get("required")),
                booleanValue(schemaMap.get("additionalProperties")),
                mapValue(schemaMap.get("definitions")),
                mapValue(schemaMap.get("definitions"))
        );
        return new McpSchema.Tool(
                name,
                name,
                description != null ? description : name,
                inputSchema,
                null,
                new McpSchema.ToolAnnotations(name, false, false, false, false, null),
                toolSpecification == null ? null : toolSpecification.metadata()
        );
    }

    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, mapValue) -> result.put(String.valueOf(key), mapValue));
            return result;
        }
        return Map.of();
    }

    private List<String> listValue(Object value) {
        if (value instanceof Collection<?> collection) {
            List<String> result = new ArrayList<>();
            for (Object item : collection) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        return List.of();
    }

    private Boolean booleanValue(Object value) {
        return value instanceof Boolean bool ? bool : null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
