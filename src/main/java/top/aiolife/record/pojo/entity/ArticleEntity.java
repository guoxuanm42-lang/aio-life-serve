package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章实体，保存个人文章的 Markdown 原文、纯文本内容和结构化元数据。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class ArticleEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 分类 ID，空值表示未分类。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long categoryId;

    /**
     * 文章标题。
     */
    private String title;

    /**
     * 文章摘要。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String summary;

    /**
     * Markdown 原文内容。
     */
    private String markdownContent;

    /**
     * 从 Markdown 原文派生的纯文本内容，用于搜索和后续 AI 分析。
     */
    private String plainTextContent;

    /**
     * 标签，第一阶段使用逗号分隔字符串保存。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String tags;

    /**
     * 状态：draft/published/archived。
     */
    private String status;

    /**
     * 文章字数，由后端根据纯文本内容计算。
     */
    private Integer wordCount;
}
