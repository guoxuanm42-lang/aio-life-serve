package top.aiolife.mcp.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 闪念关联事件保存请求体（MCP Tool）
 *
 * @author Ethan
 */
@Data
public class ThoughtSaveToolEventReq {

    @McpField(description = "事件内容")
    private String content;
}

