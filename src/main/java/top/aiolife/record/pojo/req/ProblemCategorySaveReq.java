package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 题目分类保存请求，承载分类名称和排序值。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Data
public class ProblemCategorySaveReq {

    /**
     * 分类 ID，更新和删除时使用。
     */
    private Long id;

    /**
     * 分类名称。
     */
    private String name;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
