package top.aiolife.record.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * 文章分类列表展示对象，包含虚拟分类数量和用户分类列表。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Data
public class ArticleCategoryListVO {

    /**
     * 全部文章数量。
     */
    private Long totalCount;

    /**
     * 未分类文章数量。
     */
    private Long uncategorizedCount;

    /**
     * 用户自定义分类列表。
     */
    private List<ArticleCategoryVO> categories;
}
