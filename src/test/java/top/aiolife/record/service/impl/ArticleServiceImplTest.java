package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.mapper.IArticleCategoryMapper;
import top.aiolife.record.mapper.IArticleMapper;
import top.aiolife.record.pojo.entity.ArticleEntity;
import top.aiolife.record.pojo.req.ArticleQueryReq;
import top.aiolife.record.pojo.req.ArticleSaveReq;
import top.aiolife.record.pojo.vo.ArticleDetailVO;
import top.aiolife.record.pojo.vo.ArticleListVO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 文章服务测试，验证文章写入校验、AI 分析底座字段派生和查询分页规则。
 *
 * @author Ethan
 * @date 2026-06-24
 */
class ArticleServiceImplTest {

    @Test
    void shouldRejectBlankTitleWhenCreatingArticle() {
        ArticleServiceImpl service = newService(mock(IArticleMapper.class), mock(IArticleCategoryMapper.class));
        ArticleSaveReq req = validReq();
        req.setTitle(" ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(req, 1L));

        assertEquals("文章标题不能为空", exception.getMessage());
    }

    @Test
    void shouldRejectBlankMarkdownContentWhenCreatingArticle() {
        ArticleServiceImpl service = newService(mock(IArticleMapper.class), mock(IArticleCategoryMapper.class));
        ArticleSaveReq req = validReq();
        req.setMarkdownContent(" ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(req, 1L));

        assertEquals("Markdown 正文不能为空", exception.getMessage());
    }

    @Test
    void shouldCreateArticleWithDefaultStatusPlainTextAndWordCount() {
        IArticleMapper articleMapper = mock(IArticleMapper.class);
        ArticleServiceImpl service = newService(articleMapper, mock(IArticleCategoryMapper.class));
        ArticleSaveReq req = validReq();
        req.setStatus(null);
        req.setMarkdownContent("# 标题\n\n这是一篇 [文章](https://example.com)。\n\n```java\nclass Demo {}\n```\n\n- item");

        ArticleDetailVO created = service.create(req, 7L);

        assertEquals("draft", created.getStatus());
        assertEquals("标题 这是一篇 文章。 item", created.getPlainTextContent());
        assertEquals(10, created.getWordCount());
        assertNotNull(created.getCreateTime());
        verify(articleMapper).insert(any(ArticleEntity.class));
    }

    @Test
    void shouldRejectIllegalStatus() {
        ArticleServiceImpl service = newService(mock(IArticleMapper.class), mock(IArticleCategoryMapper.class));
        ArticleSaveReq req = validReq();
        req.setStatus("done");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(req, 1L));

        assertEquals("文章状态不合法", exception.getMessage());
    }

    @Test
    void shouldRejectMissingCategoryWhenCreatingArticle() {
        IArticleCategoryMapper categoryMapper = mock(IArticleCategoryMapper.class);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        ArticleServiceImpl service = newService(mock(IArticleMapper.class), categoryMapper);
        ArticleSaveReq req = validReq();
        req.setCategoryId(100L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(req, 1L));

        assertEquals("文章分类不存在或无权访问", exception.getMessage());
    }

    @Test
    void shouldCheckOwnershipBeforeUpdatingArticle() {
        IArticleMapper articleMapper = mock(IArticleMapper.class);
        ArticleServiceImpl service = newService(articleMapper, mock(IArticleCategoryMapper.class));
        ArticleSaveReq req = validReq();
        req.setId(10L);
        when(articleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.update(req, 1L));

        assertEquals("文章不存在或无权访问", exception.getMessage());
    }

    @Test
    void shouldLogicDeleteOwnedArticle() {
        IArticleMapper articleMapper = mock(IArticleMapper.class);
        ArticleServiceImpl service = newService(articleMapper, mock(IArticleCategoryMapper.class));
        ArticleEntity entity = ownedArticle();
        when(articleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);

        service.delete(10L, 1L);

        ArgumentCaptor<ArticleEntity> captor = ArgumentCaptor.forClass(ArticleEntity.class);
        verify(articleMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getIsDeleted());
        assertEquals(1L, captor.getValue().getUpdateUser());
    }

    @Test
    void shouldUseDefaultPageAndLimitPageSizeWhenQueryingArticles() {
        IArticleMapper articleMapper = mock(IArticleMapper.class);
        ArticleServiceImpl service = newService(articleMapper, mock(IArticleCategoryMapper.class));
        Page<ArticleEntity> page = new Page<>(1, 200);
        page.setRecords(List.of(ownedArticle()));
        page.setTotal(1);
        when(articleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        ArticleQueryReq req = new ArticleQueryReq();
        req.setPage(0);
        req.setPageSize(500);

        PageResp<ArticleListVO> resp = service.query(req, 1L);

        ArgumentCaptor<Page<ArticleEntity>> captor = ArgumentCaptor.forClass(Page.class);
        verify(articleMapper).selectPage(captor.capture(), any(LambdaQueryWrapper.class));
        assertEquals(1L, captor.getValue().getCurrent());
        assertEquals(200L, captor.getValue().getSize());
        assertEquals(1L, resp.getTotal());
        assertEquals("文章标题", resp.getItems().get(0).getTitle());
    }

    private ArticleServiceImpl newService(IArticleMapper articleMapper, IArticleCategoryMapper categoryMapper) {
        return new ArticleServiceImpl(articleMapper, categoryMapper);
    }

    private ArticleSaveReq validReq() {
        ArticleSaveReq req = new ArticleSaveReq();
        req.setTitle("文章标题");
        req.setSummary("摘要");
        req.setMarkdownContent("# 标题\n\n正文");
        req.setTags("技术,Markdown");
        req.setStatus("draft");
        return req;
    }

    private ArticleEntity ownedArticle() {
        ArticleEntity entity = new ArticleEntity();
        entity.setId(10L);
        entity.setUserId(1L);
        entity.setTitle("文章标题");
        entity.setSummary("摘要");
        entity.setMarkdownContent("# 标题\n\n正文");
        entity.setPlainTextContent("标题 正文");
        entity.setTags("技术");
        entity.setStatus("draft");
        entity.setWordCount(4);
        return entity;
    }
}
