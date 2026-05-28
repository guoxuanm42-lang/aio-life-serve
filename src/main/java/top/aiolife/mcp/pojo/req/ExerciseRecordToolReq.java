package top.aiolife.mcp.pojo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

import java.time.LocalDate;

/**
 * 练习记录请求类（MCP Tool）
 *
 * @author Ethan
 */
@Data
public class ExerciseRecordToolReq {

    @McpField(description = "主键ID（可选，保存时会忽略）")
    private String id;

    @McpField(description = "运动类型ID")
    private String exerciseTypeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @McpField(description = "运动日期，格式：yyyy-MM-dd")
    private LocalDate exerciseDate;

    @McpField(description = "运动次数")
    private Integer exerciseCount;

    @McpField(description = "运动描述")
    private String description;
}

