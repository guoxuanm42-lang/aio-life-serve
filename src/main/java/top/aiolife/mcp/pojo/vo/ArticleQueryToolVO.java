package top.aiolife.mcp.pojo.vo;

import lombok.Data;
import top.aiolife.record.pojo.vo.ArticleListVO;

import java.time.LocalDateTime;

/**
 * 文章 MCP 查询返回摘要，面向外部 AI 返回文章元信息但不返回正文全文。
 *
 * @author Ethan
 * @date 2026-06-25
 */
@Data
public class ArticleQueryToolVO {

    private Long id;

    private Long categoryId;

    private String title;

    private String summary;

    private String tags;

    private String status;

    private Integer wordCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 从文章列表 VO 转换为 MCP 查询摘要。
     *
     * @param source 文章列表 VO
     * @return MCP 查询摘要
     *
     * @author Ethan
     * @date 2026-06-25
     */
    public static ArticleQueryToolVO of(ArticleListVO source) {
        ArticleQueryToolVO vo = new ArticleQueryToolVO();
        vo.setId(source.getId());
        vo.setCategoryId(source.getCategoryId());
        vo.setTitle(source.getTitle());
        vo.setSummary(source.getSummary());
        vo.setTags(source.getTags());
        vo.setStatus(source.getStatus());
        vo.setWordCount(source.getWordCount());
        vo.setCreateTime(source.getCreateTime());
        vo.setUpdateTime(source.getUpdateTime());
        return vo;
    }
}
