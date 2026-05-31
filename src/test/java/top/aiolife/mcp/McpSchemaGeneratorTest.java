package top.aiolife.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import top.aiolife.mcp.annotation.McpField;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.pojo.req.ThoughtSaveToolReq;
import top.aiolife.mcp.pojo.req.TimeRecordSaveToolReq;
import top.aiolife.mcp.schema.McpFieldSchemaResolver;
import top.aiolife.mcp.schema.McpSchemaGenerator;
import top.aiolife.mcp.tools.ThoughtMcpTools;
import top.aiolife.mcp.tools.TimeRecordMcpTools;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MCP Schema 生成器测试。
 *
 * @author Ethan
 * @date 2026-05-31
 */
class McpSchemaGeneratorTest {

    private final McpSchemaGenerator generator = new McpSchemaGenerator(new McpFieldSchemaResolver());

    @Test
    void shouldGenerateThoughtSaveSchemaWithNestedEventsAndDescriptions() throws NoSuchFieldException, NoSuchMethodException {
        Method method = ThoughtMcpTools.class.getDeclaredMethod("thoughtSave", ThoughtSaveToolReq.class);
        McpSchema.Tool tool = generate(method);
        Map<String, Object> properties = tool.inputSchema().properties();

        assertTrue(properties.containsKey("subject"));
        assertTrue(properties.containsKey("content"));
        assertTrue(properties.containsKey("themeKey"));
        assertTrue(properties.containsKey("status"));
        assertTrue(properties.containsKey("events"));
        assertTrue(properties.containsKey("idempotencyKey"));

        Map<?, ?> subjectSchema = (Map<?, ?>) properties.get("subject");
        McpField subjectField = ThoughtSaveToolReq.class.getDeclaredField("subject").getAnnotation(McpField.class);
        assertEquals(subjectField.description(), subjectSchema.get("description"));

        Map<?, ?> eventsSchema = (Map<?, ?>) properties.get("events");
        Map<?, ?> eventItems = (Map<?, ?>) eventsSchema.get("items");
        Map<?, ?> eventProperties = (Map<?, ?>) eventItems.get("properties");
        assertTrue(eventProperties.containsKey("content"));
    }

    @Test
    void shouldGenerateTimeRecordSaveSchemaWithNestedExercises() throws NoSuchMethodException {
        Method method = TimeRecordMcpTools.class.getDeclaredMethod("save", TimeRecordSaveToolReq.class);
        McpSchema.Tool tool = generate(method);
        Map<String, Object> properties = tool.inputSchema().properties();

        assertTrue(properties.containsKey("categoryId"));
        assertTrue(properties.containsKey("date"));
        assertTrue(properties.containsKey("startTime"));
        assertTrue(properties.containsKey("endTime"));
        assertTrue(properties.containsKey("title"));
        assertTrue(properties.containsKey("description"));
        assertTrue(properties.containsKey("exercises"));
        assertTrue(properties.containsKey("idempotencyKey"));

        Map<?, ?> exercisesSchema = (Map<?, ?>) properties.get("exercises");
        Map<?, ?> exerciseItems = (Map<?, ?>) exercisesSchema.get("items");
        Map<?, ?> exerciseProperties = (Map<?, ?>) exerciseItems.get("properties");
        assertTrue(exerciseProperties.containsKey("exerciseTypeId"));
        assertTrue(exerciseProperties.containsKey("exerciseDate"));
        assertTrue(exerciseProperties.containsKey("exerciseCount"));
    }

    private McpSchema.Tool generate(Method method) {
        McpOperation operation = method.getAnnotation(McpOperation.class);
        return generator.generateTool(operation.name(), operation.description(), method, null, operation);
    }
}
