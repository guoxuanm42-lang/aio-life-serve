package top.aiolife.mcp.pojo.req;

import lombok.Data;

import java.util.Map;

/**
 * MCP 工具模拟调用请求体。
 *
 * <p>用途：承载研发管理页面传入的 MCP 工具调用参数。</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Data
public class McpToolCallReq {

    /**
     * MCP 工具参数。
     */
    private Map<String, Object> arguments;
}
