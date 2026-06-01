package top.aiolife.mcp.pojo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 美食记录 MCP 查询请求。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordQueryToolReq {

    @McpField(description = "当前页码，默认 1")
    private Integer page = 1;

    @McpField(description = "每页数量，默认 20，最大 200")
    private Integer pageSize = 20;

    @McpField(description = "菜名关键词，模糊查询")
    private String keyword;

    @McpField(description = "分类，例如 家常菜、汤、主食、甜品")
    private String category;

    @McpField(description = "餐次，例如 早餐、午餐、晚餐、夜宵、加餐")
    private String mealType;

    @McpField(description = "状态：draft/done/to_improve/archived")
    private String status;

    @McpField(description = "标签关键词，模糊查询")
    private String tags;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @McpField(description = "做饭开始日期，格式 yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @McpField(description = "做饭结束日期，格式 yyyy-MM-dd")
    private LocalDate endDate;

    @McpField(description = "最低评分，返回评分大于等于该值的记录")
    private BigDecimal rating;

    @McpField(description = "是否值得复做")
    private Boolean worthRedo;

    @McpField(description = "是否待优化；true 等价于 status=to_improve")
    private Boolean toImprove;
}
