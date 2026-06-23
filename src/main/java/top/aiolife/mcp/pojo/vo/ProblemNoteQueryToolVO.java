package top.aiolife.mcp.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目记录 MCP 查询结果，返回适合外部 AI 阅读的题目核心字段。
 *
 * @author Ethan
 * @date 2026-06-23
 */
@Data
public class ProblemNoteQueryToolVO {

    private Long id;

    private Long categoryId;

    private String title;

    private String problemContent;

    private String solutionCode;

    private String ideaNote;

    private String difficulty;

    private String tags;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
