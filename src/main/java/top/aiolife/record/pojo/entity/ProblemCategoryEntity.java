package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目分类实体，保存用户自定义的一层题目分类。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("problem_category")
public class ProblemCategoryEntity extends BaseEntity {

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
