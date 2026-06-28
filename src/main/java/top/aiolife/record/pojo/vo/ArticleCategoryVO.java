package top.aiolife.record.pojo.vo;

import lombok.Data;
import top.aiolife.record.pojo.entity.ArticleCategoryEntity;

/**
 * 文章分类展示对象，包含分类基础信息和文章数量。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Data
public class ArticleCategoryVO {

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
     * 分类下文章数量。
     */
    private Long articleCount;

    /**
     * 从实体创建文章分类展示对象。
     *
     * @param entity 分类实体
     * @param articleCount 文章数量
     * @return 文章分类展示对象
     *
     * @author Ethan
     * @date 2026-06-24
     */
    public static ArticleCategoryVO of(ArticleCategoryEntity entity, Long articleCount) {
        ArticleCategoryVO vo = new ArticleCategoryVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setSortOrder(entity.getSortOrder());
        vo.setArticleCount(articleCount);
        return vo;
    }
}
