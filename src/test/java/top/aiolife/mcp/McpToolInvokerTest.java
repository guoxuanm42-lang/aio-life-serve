package top.aiolife.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.invoker.McpToolInvoker;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MCP 工具调用器测试。
 *
 * @author Ethan
 * @date 2026-05-31
 */
class McpToolInvokerTest {

    private final McpToolInvoker invoker = new McpToolInvoker(new ObjectMapper());

    @Test
    void shouldUnwrapApiResponseData() throws NoSuchMethodException {
        McpSchema.CallToolResult result = invoker.invoke(
                definition("success", false, true),
                Map.of("count", 1),
                null,
                null
        );

        assertFalse(result.isError());
        assertEquals(Boolean.TRUE, result.structuredContent());
        assertEquals("true", text(result));
    }

    @Test
    void shouldReturnStringDirectly() throws NoSuchMethodException {
        McpSchema.CallToolResult result = invoker.invoke(
                definition("echo", false, true),
                Map.of("count", 1),
                null,
                null
        );

        assertFalse(result.isError());
        assertEquals("ok", result.structuredContent());
        assertEquals("ok", text(result));
    }

    @Test
    void shouldReturnErrorWhenApiResponseFailed() throws NoSuchMethodException {
        McpSchema.CallToolResult result = invoker.invoke(
                definition("failedResponse", false, true),
                Map.of("count", 1),
                null,
                null
        );

        assertTrue(result.isError());
        assertEquals("保存失败", text(result));
    }

    @Test
    void shouldReturnErrorWhenArgumentsInvalid() throws NoSuchMethodException {
        McpSchema.CallToolResult result = invoker.invoke(
                definition("success", false, true),
                Map.of("count", "abc"),
                null,
                null
        );

        assertTrue(result.isError());
        assertTrue(text(result).startsWith("参数格式错误："));
    }

    @Test
    void shouldReturnErrorWhenLoginRequiredButMissing() throws NoSuchMethodException {
        TestTool tool = new TestTool();
        McpSchema.CallToolResult result = invoker.invoke(
                definition(tool, "countCall", true, true),
                Map.of("count", 1),
                null,
                null
        );

        assertTrue(result.isError());
        assertEquals("未登录或登录已过期", text(result));
        assertEquals(0, tool.callCount.get());
    }

    @Test
    void shouldReturnBusinessMessageForRuntimeException() throws NoSuchMethodException {
        McpSchema.CallToolResult result = invoker.invoke(
                definition("runtimeError", false, true),
                Map.of("count", 1),
                null,
                null
        );

        assertTrue(result.isError());
        assertEquals("业务失败", text(result));
    }

    @Test
    void shouldHideUnknownExceptionMessage() throws NoSuchMethodException {
        McpSchema.CallToolResult result = invoker.invoke(
                definition("checkedError", false, true),
                Map.of("count", 1),
                null,
                null
        );

        assertTrue(result.isError());
        assertEquals("工具执行失败", text(result));
    }

    private McpToolDefinition definition(String methodName,
                                         boolean authRequired,
                                         boolean unwrapApiResponse) throws NoSuchMethodException {
        return definition(new TestTool(), methodName, authRequired, unwrapApiResponse);
    }

    private McpToolDefinition definition(TestTool tool,
                                         String methodName,
                                         boolean authRequired,
                                         boolean unwrapApiResponse) throws NoSuchMethodException {
        Method method = TestTool.class.getDeclaredMethod(methodName, SimpleReq.class);
        return new McpToolDefinition(
                methodName,
                methodName,
                tool,
                method,
                SimpleReq.class,
                method.getReturnType(),
                null,
                authRequired,
                unwrapApiResponse,
                null,
                null
        );
    }

    private String text(McpSchema.CallToolResult result) {
        return ((McpSchema.TextContent) result.content().getFirst()).text();
    }

    private static final class TestTool {

        private final AtomicInteger callCount = new AtomicInteger();

        public ApiResponse<Boolean> success(SimpleReq req) {
            return ApiResponse.success(true);
        }

        public String echo(SimpleReq req) {
            return "ok";
        }

        public ApiResponse<Boolean> failedResponse(SimpleReq req) {
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "保存失败");
        }

        public ApiResponse<Boolean> countCall(SimpleReq req) {
            callCount.incrementAndGet();
            return ApiResponse.success(true);
        }

        public ApiResponse<Boolean> runtimeError(SimpleReq req) {
            throw new RuntimeException("业务失败");
        }

        public ApiResponse<Boolean> checkedError(SimpleReq req) throws IOException {
            throw new IOException("底层错误");
        }
    }

    @Getter
    @Setter
    private static final class SimpleReq {

        private Integer count;
    }
}
