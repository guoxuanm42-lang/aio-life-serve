package top.aiolife.mcp.pojo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

import java.time.LocalDate;

/**
 * 闪念 MCP 查询请求。
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtQueryToolReq {

    @McpField(description = "关键词，同时模糊匹配主题、正文和关联事件内容")
    private String keyword;

    @McpField(description = "主题关键词，只模糊匹配闪念主题")
    private String subject;

    @McpField(description = "正文关键词，只模糊匹配闪念正文")
    private String content;

    @McpField(description = "分类主题色：blue/cyan/green/purple/pink/orange/teal/indigo")
    private String themeKey;

    @McpField(description = "状态：pending/ongoing/done/shelved/archived，也支持中文状态")
    private String status;

    @McpField(description = "闪念类型：action 想法行动、emotion 情绪心情、reflection 复盘沉淀；不传查询全部")
    private String thoughtType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @McpField(description = "创建开始日期，格式 yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @McpField(description = "创建结束日期，格式 yyyy-MM-dd")
    private LocalDate endDate;

    @McpField(description = "是否只查询带有关联事件的闪念；true 表示只查有事件，false 表示只查无事件")
    private Boolean hasEvents;

    @McpField(description = "当前页码，默认 1")
    private Integer page = 1;

    @McpField(description = "每页数量，默认 10，最大 50")
    private Integer pageSize = 10;

    @McpField(description = "排序字段：createTime/updateTime，默认 updateTime")
    private String sortBy;

    @McpField(description = "排序方向：desc/asc，默认 desc")
    private String sortOrder;
}
