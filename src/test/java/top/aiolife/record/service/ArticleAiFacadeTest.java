package top.aiolife.record.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.core.resq.PageResp;
import top.aiolife.mcp.pojo.req.ArticleQueryToolReq;
import top.aiolife.mcp.pojo.vo.ArticleDetailToolVO;
import top.aiolife.mcp.pojo.vo.ArticleQueryToolVO;
import top.aiolife.record.pojo.req.ArticleQueryReq;
import top.aiolife.record.pojo.req.ArticleSaveReq;
import top.aiolife.record.pojo.vo.ArticleDetailVO;
import top.aiolife.record.pojo.vo.ArticleListVO;
import top.aiolife.record.util.RedisUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 文章 MCP/AI 适配门面测试，验证查询映射、详情返回和幂等保存行为。
 *
 * @author Ethan
 * @date 2026-06-25
 */
class ArticleAiFacadeTest {

    @Test
    void shouldQueryArticlesAndReturnSummaryOnly() {
        IArticleService articleService = mock(IArticleService.class);
        ArticleAiFacade facade = new ArticleAiFacade(articleService, mock(RedisUtil.class));
        ArticleListVO article = listVO(100L);
        when(articleService.query(any(ArticleQueryReq.class), eq(1L))).thenReturn(PageResp.of(List.of(article), 1L));

        ArticleQueryToolReq req = new ArticleQueryToolReq();
        req.setKeyword("Markdown");
        req.setStatus("draft");
        req.setTags("写作");
        req.setCategoryId(10L);

        PageResp<ArticleQueryToolVO> result = facade.query(req, 1L);

        assertEquals(1L, result.getTotal());
        assertEquals("Markdown 写作规范", result.getItems().get(0).getTitle());
        ArgumentCaptor<ArticleQueryReq> captor = ArgumentCaptor.forClass(ArticleQueryReq.class);
        verify(articleService).query(captor.capture(), eq(1L));
        ArticleQueryReq queryReq = captor.getValue();
        assertEquals("Markdown", queryReq.getKeyword());
        assertEquals("draft", queryReq.getStatus());
        assertEquals("写作", queryReq.getTags());
        assertEquals(10L, queryReq.getCategoryId());
    }

    @Test
    void shouldReturnArticleDetail() {
        IArticleService articleService = mock(IArticleService.class);
        ArticleAiFacade facade = new ArticleAiFacade(articleService, mock(RedisUtil.class));
        when(articleService.detail(100L, 1L)).thenReturn(detailVO(100L));

        ArticleDetailToolVO result = facade.detail(100L, 1L);

        assertEquals(100L, result.getId());
        assertEquals("# 标题\n\n正文", result.getMarkdownContent());
        assertEquals("标题 正文", result.getPlainTextContent());
    }

    @Test
    void shouldReturnExistingArticleWhenIdempotencyKeyHit() {
        IArticleService articleService = mock(IArticleService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ArticleAiFacade facade = new ArticleAiFacade(articleService, redisUtil);
        when(redisUtil.get("mcp:idemp:article_save:1:key-1")).thenReturn("100");
        when(articleService.detail(100L, 1L)).thenReturn(detailVO(100L));

        ArticleDetailToolVO result = facade.create(new ArticleSaveReq(), 1L, "key-1");

        assertEquals(100L, result.getId());
        verify(articleService).detail(100L, 1L);
    }

    @Test
    void shouldCreateArticleAndStoreIdempotencyResult() {
        IArticleService articleService = mock(IArticleService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ArticleAiFacade facade = new ArticleAiFacade(articleService, redisUtil);
        ArticleDetailVO created = detailVO(100L);
        when(redisUtil.setIfAbsent(eq("mcp:idemp:article_save:1:key-1"), eq("LOCK"), anyLong(), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(articleService.create(any(ArticleSaveReq.class), eq(1L))).thenReturn(created);

        ArticleDetailToolVO result = facade.create(new ArticleSaveReq(), 1L, "key-1");

        assertEquals(100L, result.getId());
        verify(redisUtil).set("mcp:idemp:article_save:1:key-1", "100", TimeUnit.HOURS.toSeconds(24), TimeUnit.SECONDS);
    }

    @Test
    void shouldCreateArticleDirectlyWhenNoIdempotencyKey() {
        IArticleService articleService = mock(IArticleService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ArticleAiFacade facade = new ArticleAiFacade(articleService, redisUtil);
        when(articleService.create(any(ArticleSaveReq.class), eq(1L))).thenReturn(detailVO(100L));

        ArticleDetailToolVO result = facade.create(new ArticleSaveReq(), 1L, " ");

        assertEquals(100L, result.getId());
        verify(articleService).create(any(ArticleSaveReq.class), eq(1L));
        verifyNoInteractions(redisUtil);
    }

    @Test
    void shouldReturnNullWhenIdempotencyLockIsPending() {
        IArticleService articleService = mock(IArticleService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ArticleAiFacade facade = new ArticleAiFacade(articleService, redisUtil);
        when(redisUtil.setIfAbsent(eq("mcp:idemp:article_save:1:key-1"), eq("LOCK"), anyLong(), eq(TimeUnit.SECONDS)))
                .thenReturn(false);
        when(redisUtil.get("mcp:idemp:article_save:1:key-1")).thenReturn(null);

        ArticleDetailToolVO result = facade.create(new ArticleSaveReq(), 1L, "key-1");

        assertNull(result);
    }

    private ArticleListVO listVO(Long id) {
        ArticleListVO vo = new ArticleListVO();
        vo.setId(id);
        vo.setCategoryId(10L);
        vo.setTitle("Markdown 写作规范");
        vo.setSummary("记录 Markdown 写作结构");
        vo.setTags("写作,Markdown");
        vo.setStatus("draft");
        vo.setWordCount(20);
        return vo;
    }

    private ArticleDetailVO detailVO(Long id) {
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(id);
        vo.setCategoryId(10L);
        vo.setTitle("Markdown 写作规范");
        vo.setSummary("记录 Markdown 写作结构");
        vo.setMarkdownContent("# 标题\n\n正文");
        vo.setPlainTextContent("标题 正文");
        vo.setTags("写作,Markdown");
        vo.setStatus("draft");
        vo.setWordCount(20);
        return vo;
    }
}
