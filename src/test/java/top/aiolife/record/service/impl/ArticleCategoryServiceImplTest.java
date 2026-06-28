package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.record.mapper.IArticleCategoryMapper;
import top.aiolife.record.mapper.IArticleMapper;
import top.aiolife.record.pojo.entity.ArticleCategoryEntity;
import top.aiolife.record.pojo.req.ArticleCategorySaveReq;
import top.aiolife.record.pojo.vo.ArticleCategoryListVO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 文章分类服务测试，验证分类校验、分类计数和删除分类后的文章归属处理。
 *
 * @author Ethan
 * @date 2026-06-24
 */
class ArticleCategoryServiceImplTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), top.aiolife.record.pojo.entity.ArticleEntity.class);
    }

    @Test
    void shouldRejectBlankCategoryNameWhenCreatingCategory() {
        ArticleCategoryServiceImpl service = newService(mock(IArticleCategoryMapper.class), mock(IArticleMapper.class));
        ArticleCategorySaveReq req = new ArticleCategorySaveReq();
        req.setName(" ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(req, 1L));

        assertEquals("文章分类名称不能为空", exception.getMessage());
    }

    @Test
    void shouldListCategoriesWithCounts() {
        IArticleCategoryMapper categoryMapper = mock(IArticleCategoryMapper.class);
        IArticleMapper articleMapper = mock(IArticleMapper.class);
        ArticleCategoryServiceImpl service = newService(categoryMapper, articleMapper);
        ArticleCategoryEntity category = category(10L, "技术文章");
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(category));
        when(articleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L, 5L, 2L);

        ArticleCategoryListVO vo = service.listWithCount(1L);

        assertEquals(5L, vo.getTotalCount());
        assertEquals(2L, vo.getUncategorizedCount());
        assertEquals(1, vo.getCategories().size());
        assertEquals(3L, vo.getCategories().get(0).getArticleCount());
    }

    @Test
    void shouldRejectOtherUsersCategoryWhenUpdating() {
        IArticleCategoryMapper categoryMapper = mock(IArticleCategoryMapper.class);
        ArticleCategoryServiceImpl service = newService(categoryMapper, mock(IArticleMapper.class));
        ArticleCategorySaveReq req = new ArticleCategorySaveReq();
        req.setId(10L);
        req.setName("写作文章");
        when(categoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.update(req, 1L));

        assertEquals("文章分类不存在或无权访问", exception.getMessage());
    }

    @Test
    void shouldMoveArticlesToUncategorizedWhenDeletingCategory() {
        IArticleCategoryMapper categoryMapper = mock(IArticleCategoryMapper.class);
        IArticleMapper articleMapper = mock(IArticleMapper.class);
        ArticleCategoryServiceImpl service = newService(categoryMapper, articleMapper);
        ArticleCategoryEntity category = category(10L, "技术文章");
        when(categoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(category);

        service.delete(10L, 1L);

        verify(articleMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        ArgumentCaptor<ArticleCategoryEntity> captor = ArgumentCaptor.forClass(ArticleCategoryEntity.class);
        verify(categoryMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getIsDeleted());
        assertEquals(1L, captor.getValue().getUpdateUser());
    }

    private ArticleCategoryServiceImpl newService(IArticleCategoryMapper categoryMapper, IArticleMapper articleMapper) {
        return new ArticleCategoryServiceImpl(categoryMapper, articleMapper);
    }

    private ArticleCategoryEntity category(Long id, String name) {
        ArticleCategoryEntity entity = new ArticleCategoryEntity();
        entity.setId(id);
        entity.setUserId(1L);
        entity.setName(name);
        entity.setSortOrder(0);
        return entity;
    }
}
