package top.aiolife.mcp.schema;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.mcp.annotation.McpField;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP 字段 Schema 解析器。
 *
 * <p>用途：将 Java 类型解析为 MCP 输入参数使用的 JSON Schema 片段。</p>
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Component
public class McpFieldSchemaResolver {

    /**
     * 构建指定 Java 类型的 JSON Schema。
     *
     * @param type Java 类型
     * @param path 当前字段路径，用于匹配忽略字段
     * @param ignoreFields 需要从输入 Schema 中排除的字段路径
     * @return JSON Schema map
     *
     * @author Ethan
     * @date 2026-05-31
     */
    public Map<String, Object> buildSchema(Type type, String path, Set<String> ignoreFields) {
        if (type instanceof GenericArrayType genericArrayType) {
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "array");
            schema.put("items", buildSchema(genericArrayType.getGenericComponentType(), appendArray(path), ignoreFields));
            return schema;
        }

        Class<?> rawClass = rawClass(type);
        if (rawClass == null) {
            return objectSchema(Map.of(), List.of(), true);
        }
        if (isIntegerType(rawClass)) {
            return primitiveSchema("integer");
        }
        if (isNumberType(rawClass)) {
            return primitiveSchema("number");
        }
        if (isBooleanType(rawClass)) {
            return primitiveSchema("boolean");
        }
        if (isStringLikeType(rawClass)) {
            return primitiveSchema("string");
        }
        if (rawClass.isEnum()) {
            Map<String, Object> schema = primitiveSchema("string");
            List<String> enumValues = new ArrayList<>();
            for (Object enumConstant : rawClass.getEnumConstants()) {
                enumValues.add(((Enum<?>) enumConstant).name());
            }
            schema.put("enum", enumValues);
            return schema;
        }
        if (rawClass.isArray()) {
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "array");
            schema.put("items", buildSchema(rawClass.getComponentType(), appendArray(path), ignoreFields));
            return schema;
        }
        if (Collection.class.isAssignableFrom(rawClass)) {
            Type itemType = Object.class;
            if (type instanceof ParameterizedType parameterizedType && parameterizedType.getActualTypeArguments().length > 0) {
                itemType = parameterizedType.getActualTypeArguments()[0];
            }
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "array");
            schema.put("items", buildSchema(itemType, appendArray(path), ignoreFields));
            return schema;
        }
        if (Map.class.isAssignableFrom(rawClass) || Object.class.equals(rawClass)) {
            return objectSchema(Map.of(), List.of(), true);
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (Field field : collectFields(rawClass)) {
            String fieldPath = path.isEmpty() ? field.getName() : path + "." + field.getName();
            if (ignoreFields.contains(fieldPath)) {
                continue;
            }
            Map<String, Object> fieldSchema = buildSchema(field.getGenericType(), fieldPath, ignoreFields);
            McpField mcpField = field.getAnnotation(McpField.class);
            if (mcpField != null && StringUtils.hasText(mcpField.description())) {
                fieldSchema.put("description", mcpField.description());
            }
            properties.put(field.getName(), fieldSchema);
            if (field.getType().isPrimitive() || (mcpField != null && mcpField.required())) {
                required.add(field.getName());
            }
        }
        return objectSchema(properties, required, false);
    }

    private List<Field> collectFields(Class<?> type) {
        LinkedHashMap<String, Field> fields = new LinkedHashMap<>();
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = type;
        while (current != null && !Object.class.equals(current)) {
            hierarchy.add(0, current);
            current = current.getSuperclass();
        }
        for (Class<?> hierarchyType : hierarchy) {
            for (Field field : hierarchyType.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                fields.putIfAbsent(field.getName(), field);
            }
        }
        return new ArrayList<>(fields.values());
    }

    private Map<String, Object> primitiveSchema(String type) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", type);
        return schema;
    }

    private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required, boolean additionalProperties) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        schema.put("additionalProperties", additionalProperties);
        schema.put("definitions", Map.of());
        return schema;
    }

    private String appendArray(String path) {
        return path.isEmpty() ? "[]" : path + "[]";
    }

    private Class<?> rawClass(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> clazz) {
                return clazz;
            }
        }
        return null;
    }

    private boolean isIntegerType(Class<?> type) {
        return type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == Byte.class
                || type == Short.class
                || type == Integer.class
                || type == Long.class;
    }

    private boolean isNumberType(Class<?> type) {
        return type == float.class
                || type == double.class
                || type == Float.class
                || type == Double.class
                || Number.class.isAssignableFrom(type);
    }

    private boolean isBooleanType(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    private boolean isStringLikeType(Class<?> type) {
        return CharSequence.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type)
                || LocalDate.class.isAssignableFrom(type)
                || LocalTime.class.isAssignableFrom(type)
                || LocalDateTime.class.isAssignableFrom(type)
                || OffsetDateTime.class.isAssignableFrom(type)
                || ZonedDateTime.class.isAssignableFrom(type)
                || java.time.temporal.Temporal.class.isAssignableFrom(type);
    }
}
