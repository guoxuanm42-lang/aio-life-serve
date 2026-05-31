package top.aiolife.mcp.invoker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.mcp.auth.McpSaTokenScope;
import top.aiolife.mcp.definition.McpToolDefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具调用器。
 *
 * <p>用途：统一执行已注册 MCP 工具，负责登录上下文透传、参数转换、ApiResponse 解包和错误包装。</p>
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolInvoker {

    private static final String LOGIN_REQUIRED_MESSAGE = "未登录或登录已过期";
    private static final String DEFAULT_ERROR_MESSAGE = "工具执行失败";

    private final ObjectMapper objectMapper;

    /**
     * 调用指定 MCP 工具。
     *
     * @param definition MCP 工具定义
     * @param arguments MCP 客户端传入的参数
     * @param loginId 当前登录用户 id
     * @param requestAttributes 当前请求上下文
     * @return MCP 工具调用结果
     *
     * @author Ethan
     * @date 2026-05-31
     */
    public McpSchema.CallToolResult invoke(McpToolDefinition definition,
                                           Map<String, Object> arguments,
                                           Object loginId,
                                           RequestAttributes requestAttributes) {
        if (definition.authRequired() && loginId == null) {
            return errorResult(LOGIN_REQUIRED_MESSAGE);
        }

        try {
            Object input = convertArguments(definition, arguments);
            Object rawResult = McpSaTokenScope.runWithContext(
                    loginId,
                    requestAttributes,
                    () -> invokeTool(definition, input)
            );
            return successResult(normalizeResult(definition, rawResult));
        } catch (McpToolBusinessException exception) {
            return errorResult(exception.getMessage());
        } catch (IllegalArgumentException exception) {
            return errorResult("参数格式错误：" + safeMessage(exception, DEFAULT_ERROR_MESSAGE));
        } catch (InvocationTargetException exception) {
            return handleToolException(definition, exception.getTargetException());
        } catch (Exception exception) {
            log.error("MCP 工具调用失败: {}", definition.name(), exception);
            return errorResult(DEFAULT_ERROR_MESSAGE);
        }
    }

    private Object convertArguments(McpToolDefinition definition, Map<String, Object> arguments) {
        return objectMapper.convertValue(arguments == null ? Map.of() : arguments, definition.inputType());
    }

    private Object invokeTool(McpToolDefinition definition,
                              Object input) throws IllegalAccessException, InvocationTargetException {
        definition.method().setAccessible(true);
        return definition.method().invoke(definition.bean(), input);
    }

    private McpSchema.CallToolResult handleToolException(McpToolDefinition definition, Throwable throwable) {
        if (throwable instanceof IllegalArgumentException || throwable instanceof RuntimeException) {
            return errorResult(safeMessage(throwable, DEFAULT_ERROR_MESSAGE));
        }
        log.error("MCP 工具执行异常: {}", definition.name(), throwable);
        return errorResult(DEFAULT_ERROR_MESSAGE);
    }

    private Object normalizeResult(McpToolDefinition definition, Object rawResult) {
        if (rawResult instanceof ApiResponse<?> apiResponse) {
            if (!ResponseCodeConst.RSCODE_SUCCESS.equals(apiResponse.getRscode())) {
                throw new McpToolBusinessException(safeMessage(apiResponse.getResult(), "业务处理失败"));
            }
            return definition.unwrapApiResponse() ? apiResponse.getData() : apiResponse;
        }
        return rawResult;
    }

    private McpSchema.CallToolResult successResult(Object result) throws JsonProcessingException {
        return new McpSchema.CallToolResult(
                List.of(new McpSchema.TextContent(toText(result))),
                false,
                result,
                null
        );
    }

    private String toText(Object result) throws JsonProcessingException {
        if (result instanceof String value) {
            return value;
        }
        return objectMapper.writeValueAsString(result);
    }

    private McpSchema.CallToolResult errorResult(String errorMessage) {
        return McpSchema.CallToolResult.builder()
                .addTextContent(safeMessage(errorMessage, DEFAULT_ERROR_MESSAGE))
                .isError(true)
                .build();
    }

    private String safeMessage(Throwable throwable, String fallback) {
        return throwable == null ? fallback : safeMessage(throwable.getMessage(), fallback);
    }

    private String safeMessage(String message, String fallback) {
        return StringUtils.hasText(message) ? message : fallback;
    }

    private static final class McpToolBusinessException extends RuntimeException {

        private McpToolBusinessException(String message) {
            super(message);
        }
    }
}
