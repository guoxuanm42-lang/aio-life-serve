package top.aiolife.mcp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.mcp.mapper.IMcpToolCallLogMapper;
import top.aiolife.mcp.pojo.entity.McpToolCallLogEntity;
import top.aiolife.mcp.service.IMcpToolCallLogService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MCP tool call audit log service implementation.
 *
 * <p>Masks sensitive arguments before persisting tool call audit logs.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolCallLogServiceImpl extends ServiceImpl<IMcpToolCallLogMapper, McpToolCallLogEntity>
        implements IMcpToolCallLogService {

    private static final int ARGUMENTS_SUMMARY_LIMIT = 2000;
    private static final int ERROR_MESSAGE_LIMIT = 1000;
    private static final String MASK_VALUE = "***";

    private final IMcpToolCallLogMapper mcpToolCallLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * Records one MCP tool call audit log.
     *
     * @param toolName tool name
     * @param userId login user id
     * @param arguments original call arguments
     * @param success whether the call succeeded
     * @param errorMessage error message for failed calls
     * @param durationMs call duration in milliseconds
     * @return saved audit log entity
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Override
    public McpToolCallLogEntity record(String toolName,
                                       Object userId,
                                       Map<String, Object> arguments,
                                       boolean success,
                                       String errorMessage,
                                       long durationMs) {
        McpToolCallLogEntity entity = new McpToolCallLogEntity();
        entity.setToolName(toolName);
        entity.setUserId(parseUserId(userId));
        entity.setArgumentsSummary(toArgumentsSummary(arguments));
        entity.setSuccess(success);
        entity.setErrorMessage(truncate(errorMessage, ERROR_MESSAGE_LIMIT));
        entity.setDurationMs(durationMs);
        entity.setCreateTime(LocalDateTime.now());
        mcpToolCallLogMapper.insert(entity);
        return entity;
    }

    /**
     * Lists recent audit logs for one MCP tool.
     *
     * @param toolName tool name
     * @param limit maximum rows to return
     * @return recent call audit logs ordered by creation time descending
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Override
    public List<McpToolCallLogEntity> listRecentLogs(String toolName, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        LambdaQueryWrapper<McpToolCallLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(McpToolCallLogEntity::getToolName, toolName);
        wrapper.orderByDesc(McpToolCallLogEntity::getCreateTime);
        wrapper.last("limit " + safeLimit);
        return mcpToolCallLogMapper.selectList(wrapper);
    }

    private String toArgumentsSummary(Map<String, Object> arguments) {
        try {
            return truncate(objectMapper.writeValueAsString(maskValue(arguments)), ARGUMENTS_SUMMARY_LIMIT);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize MCP tool arguments summary", exception);
            return "{}";
        }
    }

    private Object maskValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> masked = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                masked.put(key, isSensitiveKey(key) ? MASK_VALUE : maskValue(entry.getValue()));
            }
            return masked;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> masked = new ArrayList<>();
            for (Object item : iterable) {
                masked.add(maskValue(item));
            }
            return masked;
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.contains("token")
                || normalized.contains("key")
                || normalized.contains("secret")
                || normalized.contains("authorization")
                || normalized.contains("apikey")
                || normalized.contains("accesstoken")
                || normalized.contains("refreshtoken");
    }

    private Long parseUserId(Object userId) {
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String text && StringUtils.hasText(text)) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException exception) {
                return null;
            }
        }
        return null;
    }

    private String truncate(String value, int limit) {
        if (value == null || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit);
    }
}
