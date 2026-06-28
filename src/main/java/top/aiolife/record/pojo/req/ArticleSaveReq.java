package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 文章保存请求，承载文章标题、Markdown 正文和结构化元数据。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Data
public class ArticleSaveReq {

    /**
     * 文章 ID，更新时必填。
     */
    private Long id;

    /**
     * 分类 ID，空值表示未分类。
     */
    private Long categoryId;

    /**
     * 文章标题。
     */
    private String title;

    /**
     * 文章摘要。
     */
    private String summary;

    /**
     * Markdown 原文内容。
     */
    private String markdownContent;

    /**
     * 标签，第一阶段使用逗号分隔字符串保存。
     */
    private String tags;

    /**
     * 状态：draft/published/archived。
     */
    private String status;
}
