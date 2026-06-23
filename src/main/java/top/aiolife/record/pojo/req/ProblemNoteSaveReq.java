package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 题目记录保存请求，承载题目内容、Java 解法代码和思路备注。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Data
public class ProblemNoteSaveReq {

    /**
     * 题目记录 ID，更新时必填。
     */
    private Long id;

    /**
     * 分类 ID，空值表示未分类。
     */
    private Long categoryId;

    /**
     * 题目标题。
     */
    private String title;

    /**
     * 题目内容。
     */
    private String problemContent;

    /**
     * Java 解法代码。
     */
    private String solutionCode;

    /**
     * 解题思路备注。
     */
    private String ideaNote;

    /**
     * 题目难度。
     */
    private String difficulty;

    /**
     * 标签，第一阶段用逗号分隔字符串保存。
     */
    private String tags;

    /**
     * 状态：draft/solved/reviewing/archived。
     */
    private String status;
}
