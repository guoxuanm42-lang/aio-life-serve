package top.aiolife.mcp.pojo.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

import java.time.LocalDateTime;

/**
 * 闪念关联事件保存请求体（MCP Tool）
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtSaveToolEventReq {

    @McpField(description = "事件内容")
    private String content;

    @JsonAlias({"eventTime", "recordTime", "happenedAt"})
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @McpField(description = "事件发生时间，格式 yyyy-MM-dd HH:mm:ss；eventTime、recordTime 和 happenedAt 是该字段别名")
    private LocalDateTime createTime;
}

