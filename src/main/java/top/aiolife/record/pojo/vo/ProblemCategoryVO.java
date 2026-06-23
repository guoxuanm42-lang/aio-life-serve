package top.aiolife.record.pojo.vo;

import lombok.Data;
import top.aiolife.record.pojo.entity.ProblemCategoryEntity;

/**
 * 题目分类展示对象，包含分类基础信息和题目数量。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Data
public class ProblemCategoryVO {

    /**
     * 分类 ID。
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

    /**
     * 分类下题目数量。
     */
    private Long problemCount;

    /**
     * 从实体创建分类展示对象。
     *
     * @param entity 分类实体
     * @param problemCount 题目数量
     * @return 分类展示对象
     *
     * @author Ethan
     * @date 2026-06-22
     */
    public static ProblemCategoryVO of(ProblemCategoryEntity entity, Long problemCount) {
        ProblemCategoryVO vo = new ProblemCategoryVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setSortOrder(entity.getSortOrder());
        vo.setProblemCount(problemCount);
        return vo;
    }
}
