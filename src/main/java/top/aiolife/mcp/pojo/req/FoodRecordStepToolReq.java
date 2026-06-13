package top.aiolife.mcp.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 美食记录步骤 MCP 轻量保存请求。
 *
 * @author Ethan
 * @date 2026-06-10
 */
@Data
public class FoodRecordStepToolReq {

    @McpField(description = "步骤标题，例如 备菜、煎蛋、收汁")
    private String title;

    @McpField(description = "步骤描述，记录该步骤的具体操作")
    private String description;

    @McpField(description = "该步骤耗时，单位分钟；可选")
    private Integer durationMinutes;
}
