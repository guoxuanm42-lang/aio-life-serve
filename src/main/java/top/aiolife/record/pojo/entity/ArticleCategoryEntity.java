package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章分类实体，保存用户自定义的一层文章分类。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_category")
public class ArticleCategoryEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 分类名称。
     */
    private String name;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
