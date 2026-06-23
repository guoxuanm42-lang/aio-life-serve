package top.aiolife.record.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * 题目分类列表展示对象，包含虚拟分类数量和用户分类列表。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Data
public class ProblemCategoryListVO {

    /**
     * 全部题目数量。
     */
    private Long totalCount;

    /**
     * 未分类题目数量。
     */
    private Long uncategorizedCount;

    /**
     * 用户自定义分类列表。
     */
    private List<ProblemCategoryVO> categories;
}
