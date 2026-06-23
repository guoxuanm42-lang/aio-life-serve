package top.aiolife.mcp.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 题目记录 MCP 查询请求，承载分页、关键词、分类和状态筛选条件。
 *
 * @author Ethan
 * @date 2026-06-23
 */
@Data
public class ProblemNoteQueryToolReq {

    @McpField(description = "当前页码，默认 1")
    private Integer page = 1;

    @McpField(description = "每页数量，默认 50，最大 200")
    private Integer pageSize = 50;

    @McpField(description = "关键词，模糊匹配题目标题、题目内容和解题思路")
    private String keyword;

    @McpField(description = "题目难度，例如 easy/medium/hard 或自定义难度文本")
    private String difficulty;

    @McpField(description = "题目状态：draft/solved/reviewing/archived")
    private String status;

    @McpField(description = "标签关键词，模糊匹配逗号分隔的标签字符串")
    private String tags;

    @McpField(description = "题目分类 ID；为空表示不按分类筛选")
    private Long categoryId;

    @McpField(description = "是否只查询未分类题目；true 时忽略 categoryId")
    private Boolean uncategorized;
}
