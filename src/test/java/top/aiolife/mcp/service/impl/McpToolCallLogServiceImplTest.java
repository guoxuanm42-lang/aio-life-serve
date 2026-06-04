package top.aiolife.mcp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.mcp.mapper.IMcpToolCallLogMapper;
import top.aiolife.mcp.pojo.entity.McpToolCallLogEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MCP tool call audit log service implementation test.
 *
 * @author Ethan
 * @date 2026-06-03
 */
class McpToolCallLogServiceImplTest {

    /**
     * Verifies audit log arguments are recursively masked before persistence.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldMaskSensitiveArgumentsBeforeRecord() {
        IMcpToolCallLogMapper mapper = mock(IMcpToolCallLogMapper.class);
        when(mapper.insert(any(McpToolCallLogEntity.class))).thenReturn(1);
        McpToolCallLogServiceImpl service = new McpToolCallLogServiceImpl(mapper, new ObjectMapper());

        service.record(
                "tool_save",
                "1001",
                Map.of(
                        "title", "demo",
                        "password", "plain",
                        "nested", Map.of("apiKey", "secret-value"),
                        "items", List.of(Map.of("accessToken", "token-value"))
                ),
                true,
                null,
                12L
        );

        ArgumentCaptor<McpToolCallLogEntity> captor = ArgumentCaptor.forClass(McpToolCallLogEntity.class);
        verify(mapper).insert(captor.capture());
        McpToolCallLogEntity entity = captor.getValue();
        assertEquals("tool_save", entity.getToolName());
        assertEquals(1001L, entity.getUserId());
        assertTrue(entity.getSuccess());
        assertTrue(entity.getArgumentsSummary().contains("\"password\":\"***\""));
        assertTrue(entity.getArgumentsSummary().contains("\"apiKey\":\"***\""));
        assertTrue(entity.getArgumentsSummary().contains("\"accessToken\":\"***\""));
        assertFalse(entity.getArgumentsSummary().contains("plain"));
        assertFalse(entity.getArgumentsSummary().contains("secret-value"));
        assertFalse(entity.getArgumentsSummary().contains("token-value"));
    }

    /**
     * Verifies recent log query caps the requested limit.
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @Test
    void shouldCapRecentLogLimit() {
        IMcpToolCallLogMapper mapper = mock(IMcpToolCallLogMapper.class);
        McpToolCallLogServiceImpl service = new McpToolCallLogServiceImpl(mapper, new ObjectMapper());

        service.listRecentLogs("tool_save", 500);

        verify(mapper).selectList(any());
    }
}
