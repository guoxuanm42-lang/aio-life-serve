package top.aiolife.mcp.pojo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

import java.time.LocalDate;
import java.util.List;

/**
 * 时迹保存请求体（MCP Tool）
 *
 * @author Ethan
 */
@Data
public class TimeRecordSaveToolReq {

    @McpField(description = "主键ID（可选，保存时会忽略）")
    private String id;

    @McpField(description = "分类ID")
    private String categoryId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @McpField(description = "日期，格式：yyyy-MM-dd")
    private LocalDate date;

    @McpField(description = "开始时间（从 0:00 开始的分钟数）")
    private Integer startTime;

    @McpField(description = "结束时间（从 0:00 开始的分钟数）")
    private Integer endTime;

    @McpField(description = "标题（可选）")
    private String title;

    @McpField(description = "描述（可选）")
    private String description;

    @McpField(description = "关联的练习记录列表（可选）")
    private List<ExerciseRecordToolReq> exercises;

    @McpField(description = "幂等键（可选，建议 UUID；重复调用将只写入一次）")
    private String idempotencyKey;
}

