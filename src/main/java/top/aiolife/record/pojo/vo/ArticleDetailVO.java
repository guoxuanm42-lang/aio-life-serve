package top.aiolife.record.pojo.vo;

import lombok.Data;
import top.aiolife.record.pojo.entity.ArticleEntity;

import java.time.LocalDateTime;

/**
 * 文章详情展示对象，返回 Markdown 原文和 AI 分析可用的纯文本内容。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Data
public class ArticleDetailVO {

    /**
     * 文章 ID。
     */
    private Long id;

    /**
     * 分类 ID。
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
     * 纯文本内容。
     */
    private String plainTextContent;

    /**
     * 标签。
     */
    private String tags;

    /**
     * 状态。
     */
    private String status;

    /**
     * 字数。
     */
    private Integer wordCount;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 从文章实体创建详情展示对象。
     *
     * @param entity 文章实体
     * @return 文章详情展示对象
     *
     * @author Ethan
     * @date 2026-06-24
     */
    public static ArticleDetailVO of(ArticleEntity entity) {
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(entity.getId());
        vo.setCategoryId(entity.getCategoryId());
        vo.setTitle(entity.getTitle());
        vo.setSummary(entity.getSummary());
        vo.setMarkdownContent(entity.getMarkdownContent());
        vo.setPlainTextContent(entity.getPlainTextContent());
        vo.setTags(entity.getTags());
        vo.setStatus(entity.getStatus());
        vo.setWordCount(entity.getWordCount());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
