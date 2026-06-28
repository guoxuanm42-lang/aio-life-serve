package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.IArticleCategoryMapper;
import top.aiolife.record.mapper.IArticleMapper;
import top.aiolife.record.pojo.entity.ArticleCategoryEntity;
import top.aiolife.record.pojo.entity.ArticleEntity;
import top.aiolife.record.pojo.req.ArticleCategorySaveReq;
import top.aiolife.record.pojo.vo.ArticleCategoryListVO;
import top.aiolife.record.pojo.vo.ArticleCategoryVO;
import top.aiolife.record.service.IArticleCategoryService;

import java.util.List;

/**
 * 文章分类服务实现，按当前用户隔离分类并维护分类下文章归属。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Service
@RequiredArgsConstructor
public class ArticleCategoryServiceImpl extends ServiceImpl<IArticleCategoryMapper, ArticleCategoryEntity> implements IArticleCategoryService {

    private final IArticleCategoryMapper articleCategoryMapper;

    private final IArticleMapper articleMapper;

    /**
     * 查询当前用户的文章分类列表和文章数量。
     *
     * @param userId 当前用户 ID
     * @return 文章分类列表展示对象
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @Override
    public ArticleCategoryListVO listWithCount(Long userId) {
        List<ArticleCategoryEntity> categories = articleCategoryMapper.selectList(new LambdaQueryWrapper<ArticleCategoryEntity>()
                .eq(ArticleCategoryEntity::getUserId, userId)
                .orderByAsc(ArticleCategoryEntity::getSortOrder)
                .orderByAsc(ArticleCategoryEntity::getCreateTime));

        List<ArticleCategoryVO> categoryVOS = categories.stream()
                .map(item -> ArticleCategoryVO.of(item, countByCategory(userId, item.getId())))
                .toList();

        ArticleCategoryListVO vo = new ArticleCategoryListVO();
        vo.setTotalCount(articleMapper.selectCount(new LambdaQueryWrapper<ArticleEntity>()
                .eq(ArticleEntity::getUserId, userId)));
        vo.setUncategorizedCount(articleMapper.selectCount(new LambdaQueryWrapper<ArticleEntity>()
                .eq(ArticleEntity::getUserId, userId)
                .isNull(ArticleEntity::getCategoryId)));
        vo.setCategories(categoryVOS);
        return vo;
    }

    /**
     * 新增当前用户的文章分类。
     *
     * @param req 分类保存请求
     * @param userId 当前用户 ID
     * @return 新增后的文章分类
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleCategoryEntity create(ArticleCategorySaveReq req, Long userId) {
        ArticleCategoryEntity entity = new ArticleCategoryEntity();
        entity.setUserId(userId);
        entity.setName(normalizeRequiredName(req));
        entity.setSortOrder(resolveSortOrder(req));
        entity.fillCreateCommonField(userId);
        articleCategoryMapper.insert(entity);
        return entity;
    }

    /**
     * 更新当前用户的文章分类。
     *
     * @param req 分类保存请求，必须包含分类 ID
     * @param userId 当前用户 ID
     * @return 更新后的文章分类
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleCategoryEntity update(ArticleCategorySaveReq req, Long userId) {
        if (req == null || req.getId() == null) {
            throw new IllegalArgumentException("文章分类 ID 不能为空");
        }
        ArticleCategoryEntity target = getOwnedCategory(req.getId(), userId);
        target.setName(normalizeRequiredName(req));
        target.setSortOrder(resolveSortOrder(req));
        target.fillUpdateCommonField(userId);
        articleCategoryMapper.updateById(target);
        return target;
    }

    /**
     * 删除当前用户的文章分类，并将该分类下文章转为未分类。
     *
     * @param id 分类 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        ArticleCategoryEntity category = getOwnedCategory(id, userId);
        articleMapper.update(null, new LambdaUpdateWrapper<ArticleEntity>()
                .set(ArticleEntity::getCategoryId, null)
                .eq(ArticleEntity::getUserId, userId)
                .eq(ArticleEntity::getCategoryId, id));
        category.setIsDeleted(StatusConst.IS_DELETE);
        category.fillUpdateCommonField(userId);
        articleCategoryMapper.updateById(category);
    }

    private Long countByCategory(Long userId, Long categoryId) {
        return articleMapper.selectCount(new LambdaQueryWrapper<ArticleEntity>()
                .eq(ArticleEntity::getUserId, userId)
                .eq(ArticleEntity::getCategoryId, categoryId));
    }

    private ArticleCategoryEntity getOwnedCategory(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("文章分类 ID 不能为空");
        }
        ArticleCategoryEntity entity = articleCategoryMapper.selectOne(new LambdaQueryWrapper<ArticleCategoryEntity>()
                .eq(ArticleCategoryEntity::getId, id)
                .eq(ArticleCategoryEntity::getUserId, userId));
        if (entity == null) {
            throw new IllegalArgumentException("文章分类不存在或无权访问");
        }
        return entity;
    }

    private String normalizeRequiredName(ArticleCategorySaveReq req) {
        if (req == null || !StringUtils.hasText(req.getName())) {
            throw new IllegalArgumentException("文章分类名称不能为空");
        }
        return req.getName().trim();
    }

    private Integer resolveSortOrder(ArticleCategorySaveReq req) {
        if (req == null || req.getSortOrder() == null) {
            return 0;
        }
        return req.getSortOrder();
    }
}
