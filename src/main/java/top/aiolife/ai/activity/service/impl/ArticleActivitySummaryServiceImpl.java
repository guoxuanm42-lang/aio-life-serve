package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.ArticleSummary;
import top.aiolife.ai.activity.service.ArticleActivitySummaryService;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.IArticleCategoryMapper;
import top.aiolife.record.mapper.IArticleMapper;
import top.aiolife.record.pojo.entity.ArticleCategoryEntity;
import top.aiolife.record.pojo.entity.ArticleEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章活动统计服务实现，按互斥口径构建新增文章、更新文章及新增分类统计。
 *
 * @author Ethan
 * @date 2026-08-13
 */
@Service
@RequiredArgsConstructor
public class ArticleActivitySummaryServiceImpl implements ArticleActivitySummaryService {

    private final IArticleMapper articleMapper;
    private final IArticleCategoryMapper articleCategoryMapper;

    /**
     * 汇总指定用户在活动周期内新增和更新的文章。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 文章活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    @Override
    public ArticleSummary summarize(Long userId, AiActivityDateRange range) {
        ActivitySummarySupport.validate(userId, range);
        List<ArticleEntity> newArticles = listNewArticles(userId, range);
        List<ArticleEntity> updatedArticles = listUpdatedArticles(userId, range);
        Map<Long, String> categoryNames = loadCategoryNames(newArticles, userId);

        ArticleSummary summary = new ArticleSummary();
        summary.setNewCount(newArticles.size());
        summary.setUpdatedCount(updatedArticles.size());
        summary.setNewTitles(ActivitySummarySupport.detailTexts(newArticles, ArticleEntity::getTitle));
        summary.setUpdatedTitles(ActivitySummarySupport.detailTexts(updatedArticles, ArticleEntity::getTitle));
        List<String> categoryKeys = newArticles.stream()
                .map(item -> normalizeCategoryKey(item.getCategoryId(), categoryNames))
                .toList();
        summary.setCategoryDistribution(ActivitySummarySupport.buildDistribution(
                ActivitySummarySupport.countKeys(categoryKeys),
                key -> resolveCategoryName(key, categoryNames)));
        return summary;
    }

    private List<ArticleEntity> listNewArticles(Long userId, AiActivityDateRange range) {
        LambdaQueryWrapper<ArticleEntity> wrapper = baseWrapper(userId);
        wrapper.ge(ArticleEntity::getCreateTime, range.getStartTime());
        wrapper.lt(ArticleEntity::getCreateTime, range.getEndTime());
        wrapper.orderByDesc(ArticleEntity::getCreateTime);
        return articleMapper.selectList(wrapper);
    }

    private List<ArticleEntity> listUpdatedArticles(Long userId, AiActivityDateRange range) {
        LambdaQueryWrapper<ArticleEntity> wrapper = baseWrapper(userId);
        wrapper.lt(ArticleEntity::getCreateTime, range.getStartTime());
        wrapper.ge(ArticleEntity::getUpdateTime, range.getStartTime());
        wrapper.lt(ArticleEntity::getUpdateTime, range.getEndTime());
        wrapper.orderByDesc(ArticleEntity::getUpdateTime);
        return articleMapper.selectList(wrapper);
    }

    private LambdaQueryWrapper<ArticleEntity> baseWrapper(Long userId) {
        LambdaQueryWrapper<ArticleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ArticleEntity::getTitle, ArticleEntity::getCategoryId,
                ArticleEntity::getCreateTime, ArticleEntity::getUpdateTime);
        wrapper.eq(ArticleEntity::getUserId, userId);
        wrapper.eq(ArticleEntity::getIsDeleted, StatusConst.NO_DELETE);
        return wrapper;
    }

    private Map<Long, String> loadCategoryNames(List<ArticleEntity> records, Long userId) {
        Set<Long> categoryIds = records.stream()
                .map(ArticleEntity::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<ArticleCategoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ArticleCategoryEntity::getId, ArticleCategoryEntity::getName);
        wrapper.eq(ArticleCategoryEntity::getUserId, userId);
        wrapper.eq(ArticleCategoryEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.in(ArticleCategoryEntity::getId, categoryIds);
        return articleCategoryMapper.selectList(wrapper).stream()
                .filter(item -> item.getId() != null && StringUtils.hasText(item.getName()))
                .collect(Collectors.toMap(ArticleCategoryEntity::getId, item -> item.getName().trim(), (left, right) -> left));
    }

    private String normalizeCategoryKey(Long categoryId, Map<Long, String> categoryNames) {
        return categoryId != null && categoryNames.containsKey(categoryId)
                ? categoryId.toString() : ActivitySummarySupport.UNCATEGORIZED_KEY;
    }

    private String resolveCategoryName(String key, Map<Long, String> categoryNames) {
        if (ActivitySummarySupport.UNCATEGORIZED_KEY.equals(key)) {
            return ActivitySummarySupport.UNCATEGORIZED_NAME;
        }
        return categoryNames.getOrDefault(Long.valueOf(key), ActivitySummarySupport.UNCATEGORIZED_NAME);
    }
}
