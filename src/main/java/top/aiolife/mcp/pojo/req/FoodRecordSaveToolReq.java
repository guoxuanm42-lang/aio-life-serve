package top.aiolife.mcp.pojo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 美食记录 MCP 轻量保存请求。
 *
 * @author Ethan
 * @date 2026-06-10
 */
@Data
public class FoodRecordSaveToolReq {

    @McpField(description = "美食记录 ID；为空表示创建，非空表示更新指定记录")
    private Long id;

    @McpField(description = "幂等键；创建时可选，建议 UUID，同一用户同一键重复调用只创建一次")
    private String idempotencyKey;

    @McpField(description = "菜名，必填，例如 番茄炒蛋")
    private String dishName;

    @McpField(description = "分类，例如 家常菜、汤、主食、甜品")
    private String category;

    @McpField(description = "餐次，例如 早餐、午餐、晚餐、夜宵、加餐")
    private String mealType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @McpField(description = "做饭日期，格式 yyyy-MM-dd")
    private LocalDate cookDate;

    @McpField(description = "状态：draft/done/to_improve/archived")
    private String status;

    @McpField(description = "标签，逗号分隔，例如 快手菜,下饭,待优化")
    private String tags;

    @McpField(description = "评分，建议 0-10 分，允许 1 位小数")
    private BigDecimal rating;

    @McpField(description = "材料清单；第一版 MCP 只保存文字结构，不处理图片")
    private List<FoodRecordIngredientToolReq> ingredients;

    @McpField(description = "步骤流程；第一版 MCP 只保存文字结构，不处理图片")
    private List<FoodRecordStepToolReq> steps;

    @McpField(description = "问题或不足，可以用换行或分号分隔")
    private String problems;

    @McpField(description = "本次总结，记录整体复盘")
    private String summary;

    @McpField(description = "下次改进建议")
    private String nextImprove;

    @McpField(description = "是否值得复做")
    private Boolean worthRedo;
}
