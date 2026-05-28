package top.aiolife.mcp.pojo.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

import java.util.List;

/**
 * 思考（闪念）保存请求体（MCP Tool）
 *
 * @author Ethan
 */
@Data
public class ThoughtSaveToolReq {

    @JsonAlias({"topic"})
    @McpField(description = "主题/标题（可选，未传则从 content 第一行提炼）")
    private String subject;

    @McpField(description = "内容")
    private String content;

    @McpField(description = "主题色：blue/cyan/green/purple/pink/orange（可选）")
    private String themeKey;

    @McpField(description = "状态：pending/ongoing/done/archived（可选）")
    private String status;

    @McpField(description = "关联事件列表（可选）")
    private List<ThoughtSaveToolEventReq> events;

    @McpField(description = "幂等键（可选，建议 UUID；重复调用将只写入一次）")
    private String idempotencyKey;
}

