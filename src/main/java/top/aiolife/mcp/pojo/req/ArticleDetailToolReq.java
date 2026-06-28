package top.aiolife.mcp.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 文章 MCP 详情请求，按文章 ID 查询 Markdown 原文和结构化元信息。
 *
 * @author Ethan
 * @date 2026-06-25
 */
@Data
public class ArticleDetailToolReq {

    @McpField(description = "文章 ID，必填", required = true)
    private Long id;
}
