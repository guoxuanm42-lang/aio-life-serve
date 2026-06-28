package top.aiolife.mcp.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 文章 MCP 查询请求，承载分页、关键词、分类、状态和标签筛选条件。
 *
 * @author Ethan
 * @date 2026-06-25
 */
@Data
public class ArticleQueryToolReq {

    @McpField(description = "当前页码，默认 1")
    private Integer page = 1;

    @McpField(description = "每页数量，默认 50，最大 200")
    private Integer pageSize = 50;

    @McpField(description = "关键词，模糊匹配文章标题、摘要、纯文本正文和标签")
    private String keyword;

    @McpField(description = "文章状态：draft/published/archived")
    private String status;

    @McpField(description = "标签关键词，模糊匹配逗号分隔的标签字符串")
    private String tags;

    @McpField(description = "文章分类 ID；为空表示不按分类筛选")
    private Long categoryId;

    @McpField(description = "是否只查询未分类文章；true 时忽略 categoryId")
    private Boolean uncategorized;
}
