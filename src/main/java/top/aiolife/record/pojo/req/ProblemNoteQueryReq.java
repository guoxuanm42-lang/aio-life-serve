package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 题目记录分页查询请求，承载关键词、难度、状态和标签筛选条件。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Data
public class ProblemNoteQueryReq {

    /**
     * 当前页码。
     */
    private Integer page = 1;

    /**
     * 每页数量。
     */
    private Integer pageSize = 50;

    /**
     * 关键词，匹配标题、题目内容和思路备注。
     */
    private String keyword;

    /**
     * 难度。
     */
    private String difficulty;

    /**
     * 状态。
     */
    private String status;

    /**
     * 标签关键词。
     */
    private String tags;

    /**
     * 分类 ID。
     */
    private Long categoryId;

    /**
     * 是否只查询未分类题目。
     */
    private Boolean uncategorized;
}
