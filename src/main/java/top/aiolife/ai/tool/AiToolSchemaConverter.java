package top.aiolife.ai.tool;

import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonEnumSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 工具 JSON Schema 转换器。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Component
public class AiToolSchemaConverter {

    /**
     * 将 MCP 字段 schema map 转换为 LangChain4j 对象 schema。
     *
     * @param schemaMap MCP 字段 schema map
     * @return LangChain4j 对象 schema
     *
     * @author Ethan
     * @date 2026-06-28
     */
    public JsonObjectSchema toObjectSchema(Map<String, Object> schemaMap) {
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();
        builder.addProperties(readProperties(schemaMap.get("properties")));
        builder.required(readStringList(schemaMap.get("required")));
        builder.additionalProperties(readBoolean(schemaMap.get("additionalProperties")));
        return builder.build();
    }

    private Map<String, JsonSchemaElement> readProperties(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, JsonSchemaElement> properties = new LinkedHashMap<>();
        map.forEach((key, rawSchema) -> properties.put(String.valueOf(key), toSchemaElement(rawSchema)));
        return properties;
    }

    private JsonSchemaElement toSchemaElement(Object rawSchema) {
        if (!(rawSchema instanceof Map<?, ?> map)) {
            return JsonObjectSchema.builder().additionalProperties(true).build();
        }
        String type = stringValue(map.get("type"));
        String description = stringValue(map.get("description"));
        if (map.get("enum") instanceof List<?> values) {
            return JsonEnumSchema.builder()
                    .description(description)
                    .enumValues(values.stream().map(String::valueOf).toList())
                    .build();
        }
        if ("string".equals(type)) {
            return JsonStringSchema.builder().description(description).build();
        }
        if ("integer".equals(type)) {
            return JsonIntegerSchema.builder().description(description).build();
        }
        if ("number".equals(type)) {
            return JsonNumberSchema.builder().description(description).build();
        }
        if ("boolean".equals(type)) {
            return JsonBooleanSchema.builder().description(description).build();
        }
        if ("array".equals(type)) {
            return JsonArraySchema.builder()
                    .description(description)
                    .items(toSchemaElement(map.get("items")))
                    .build();
        }
        if ("object".equals(type)) {
            JsonObjectSchema.Builder builder = JsonObjectSchema.builder().description(description);
            builder.addProperties(readProperties(map.get("properties")));
            builder.required(readStringList(map.get("required")));
            builder.additionalProperties(readBoolean(map.get("additionalProperties")));
            return builder.build();
        }
        return JsonObjectSchema.builder().description(description).additionalProperties(true).build();
    }

    private List<String> readStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private Boolean readBoolean(Object value) {
        return value instanceof Boolean bool ? bool : null;
    }

    private String stringValue(Object value) {
        String text = value == null ? null : String.valueOf(value);
        return StringUtils.hasText(text) ? text : null;
    }
}
