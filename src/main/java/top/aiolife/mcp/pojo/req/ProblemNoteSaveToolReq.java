package top.aiolife.mcp.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 题目记录 MCP 新增请求，承载外部 AI 写入题库所需的题目内容和解法信息。
 *
 * @author Ethan
 * @date 2026-06-23
 */
@Data
public class ProblemNoteSaveToolReq {

    @McpField(description = "幂等键，新增时可选，建议使用题目标题和题目内容生成稳定字符串，避免重复写入")
    private String idempotencyKey;

    @McpField(description = "题目分类 ID；为空表示保存为未分类题目")
    private Long categoryId;

    @McpField(description = "题目标题，必填", required = true)
    private String title;

    @McpField(description = "题目内容，必填，支持 Markdown 文本", required = true)
    private String problemContent;

    @McpField(description = "Java 解法代码，可选")
    private String solutionCode;

    @McpField(description = "解题思路备注，可选，支持 Markdown 文本")
    private String ideaNote;

    @McpField(description = "题目难度，例如 easy/medium/hard 或自定义难度文本")
    private String difficulty;

    @McpField(description = "标签，使用逗号分隔，例如 字符串,双指针,滑动窗口")
    private String tags;

    @McpField(description = "题目状态：draft/solved/reviewing/archived；为空时默认 draft")
    private String status;
}
