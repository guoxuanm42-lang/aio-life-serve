package top.aiolife.mcp.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 美食记录材料 MCP 轻量保存请求。
 *
 * @author Ethan
 * @date 2026-06-10
 */
@Data
public class FoodRecordIngredientToolReq {

    @McpField(description = "材料名称，例如 鸡蛋、番茄、盐")
    private String name;

    @McpField(description = "材料数量，例如 2、200、少许")
    private String quantity;

    @McpField(description = "材料单位，例如 个、克、勺")
    private String unit;

    @McpField(description = "材料备注，例如 去皮、切丁、可替换")
    private String remark;
}
