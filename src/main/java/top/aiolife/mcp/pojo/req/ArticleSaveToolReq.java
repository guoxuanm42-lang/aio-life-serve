package top.aiolife.mcp.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 文章 MCP 新增请求，承载外部 AI 写入个人文章所需的 Markdown 原文和元信息。
 *
 * @author Ethan
 * @date 2026-06-25
 */
@Data
public class ArticleSaveToolReq {

    @McpField(description = "幂等键，新增时可选但推荐；用于避免外部 AI 重试导致重复写入")
    private String idempotencyKey;

    @McpField(description = "文章分类 ID；为空表示保存为未分类文章")
    private Long categoryId;

    @McpField(description = "文章标题，必填", required = true)
    private String title;

    @McpField(description = "文章摘要，可为空；不会自动生成摘要")
    private String summary;

    @McpField(description = "Markdown 原文正文，必填；纯文本内容和字数由后端自动生成", required = true)
    private String markdownContent;

    @McpField(description = "标签，使用英文逗号分隔，例如：技术文章,Java,项目文档")
    private String tags;

    @McpField(description = "文章状态：draft/published/archived；为空时默认 draft")
    private String status;
}
