package top.aiolife.mcp.pojo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

import java.time.LocalDate;

/**
 * 时迹日期范围查询请求体（MCP Tool）
 *
 * @author Ethan
 */
@Data
public class TimeRecordDateRangeToolReq {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @McpField(description = "开始日期，格式：yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @McpField(description = "结束日期，格式：yyyy-MM-dd")
    private LocalDate endDate;
}

