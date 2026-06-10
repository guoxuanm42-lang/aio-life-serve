package top.aiolife.mcp.service.impl;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.invoker.McpToolInvoker;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.service.IMcpToolCallLogService;
import top.aiolife.mcp.service.IMcpToolCallService;
import top.aiolife.mcp.service.IMcpToolConfigService;
import top.aiolife.record.util.RedisUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MCP tool call service implementation.
 *
 * <p>Centralizes enablement checks, rate limiting, invocation, and audit logging for all MCP calls.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolCallServiceImpl implements IMcpToolCallService {

    private static final int RATE_LIMIT_WINDOW_SECONDS = 60;
    private static final int RATE_LIMIT_MAX_CALLS = 30;
    private static final String RATE_LIMIT_PREFIX = "mcp:tool:rate:";

    private final McpToolRegistry mcpToolRegistry;
    private final McpToolInvoker mcpToolInvoker;
    private final IMcpToolConfigService mcpToolConfigService;
    private final IMcpToolCallLogService mcpToolCallLogService;
    private final RedisUtil redisUtil;

    /**
     * Invokes one MCP tool with audit logging and rate limiting.
     *
     * @param name tool name
     * @param arguments tool call arguments
     * @param loginId current login user id
     * @param requestAttributes current request attributes
     * @return MCP call result
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Override
    public McpSchema.CallToolResult invoke(String name,
                                           Map<String, Object> arguments,
                                           Object loginId,
                                           RequestAttributes requestAttributes) {
        long start = System.currentTimeMillis();
        McpSchema.CallToolResult result;
        McpToolDefinition definition = mcpToolRegistry.getTool(name);
        if (definition == null) {
            result = errorResult("MCP 工具不存在: " + name);
            record(name, loginId, arguments, result, start);
            return result;
        }
        if (!mcpToolConfigService.isToolEnabled(name)) {
            result = errorResult("MCP 工具已停用: " + name);
            record(name, loginId, arguments, result, start);
            return result;
        }
        if (isRateLimited(loginId, name)) {
            result = errorResult("工具调用过于频繁，请稍后再试");
            record(name, loginId, arguments, result, start);
            return result;
        }

        result = mcpToolInvoker.invoke(definition, arguments, loginId, requestAttributes);
        record(name, loginId, arguments, result, start);
        return result;
    }

    private boolean isRateLimited(Object loginId, String toolName) {
        String userPart = loginId == null ? "anonymous" : String.valueOf(loginId);
        String key = RATE_LIMIT_PREFIX + userPart + ":" + toolName;
        try {
            Long count = redisUtil.increment(key, 1);
            if (count != null && count == 1) {
                redisUtil.expire(key, RATE_LIMIT_WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            return count != null && count > RATE_LIMIT_MAX_CALLS;
        } catch (Exception exception) {
            log.warn("MCP tool rate limit check failed, allow current call. key={}", key, exception);
            return false;
        }
    }

    private void record(String name,
                        Object loginId,
                        Map<String, Object> arguments,
                        McpSchema.CallToolResult result,
                        long start) {
        try {
            boolean success = result != null && !Boolean.TRUE.equals(result.isError());
            String errorMessage = success ? null : extractResultText(result);
            mcpToolCallLogService.record(
                    name,
                    loginId,
                    arguments,
                    success,
                    errorMessage,
                    System.currentTimeMillis() - start
            );
        } catch (Exception exception) {
            log.warn("Failed to record MCP tool call log. tool={}", name, exception);
        }
    }

    private McpSchema.CallToolResult errorResult(String message) {
        return McpSchema.CallToolResult.builder()
                .addTextContent(message)
                .isError(true)
                .build();
    }

    private String extractResultText(McpSchema.CallToolResult result) {
        if (result == null || result.content() == null) {
            return null;
        }
        List<McpSchema.Content> content = result.content();
        for (McpSchema.Content item : content) {
            if (item instanceof McpSchema.TextContent textContent) {
                return textContent.text();
            }
        }
        return null;
    }
}
