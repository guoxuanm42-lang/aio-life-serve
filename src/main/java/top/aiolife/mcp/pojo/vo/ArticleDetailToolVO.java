package top.aiolife.mcp.pojo.vo;

import lombok.Data;
import top.aiolife.record.pojo.vo.ArticleDetailVO;

import java.time.LocalDateTime;

/**
 * 文章 MCP 详情返回结果，包含 Markdown 原文和后端派生的纯文本内容。
 *
 * @author Ethan
 * @date 2026-06-25
 */
@Data
public class ArticleDetailToolVO {

    private Long id;

    private Long categoryId;

    private String title;

    private String summary;

    private String markdownContent;

    private String plainTextContent;

    private String tags;

    private String status;

    private Integer wordCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 从文章详情 VO 转换为 MCP 详情返回结果。
     *
     * @param source 文章详情 VO
     * @return MCP 详情返回结果
     *
     * @author Ethan
     * @date 2026-06-25
     */
    public static ArticleDetailToolVO of(ArticleDetailVO source) {
        ArticleDetailToolVO vo = new ArticleDetailToolVO();
        vo.setId(source.getId());
        vo.setCategoryId(source.getCategoryId());
        vo.setTitle(source.getTitle());
        vo.setSummary(source.getSummary());
        vo.setMarkdownContent(source.getMarkdownContent());
        vo.setPlainTextContent(source.getPlainTextContent());
        vo.setTags(source.getTags());
        vo.setStatus(source.getStatus());
        vo.setWordCount(source.getWordCount());
        vo.setCreateTime(source.getCreateTime());
        vo.setUpdateTime(source.getUpdateTime());
        return vo;
    }
}
