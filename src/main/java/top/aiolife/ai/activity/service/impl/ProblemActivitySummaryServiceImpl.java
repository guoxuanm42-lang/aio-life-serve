package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.ProblemSummary;
import top.aiolife.ai.activity.service.ProblemActivitySummaryService;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.IProblemCategoryMapper;
import top.aiolife.record.mapper.IProblemNoteMapper;
import top.aiolife.record.pojo.entity.ProblemCategoryEntity;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 题目活动统计服务实现，通过题目及其分类数据构建新增、分类和难度统计。
 *
 * @author Ethan
 * @date 2026-08-16
 */
@Service
@RequiredArgsConstructor
public class ProblemActivitySummaryServiceImpl implements ProblemActivitySummaryService {

    private final IProblemNoteMapper problemNoteMapper;
    private final IProblemCategoryMapper problemCategoryMapper;

    /**
     * 汇总指定用户在活动周期内新增的题目及其分类和难度分布。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 题目活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-16
     */
    @Override
    public ProblemSummary summarize(Long userId, AiActivityDateRange range) {
        ActivitySummarySupport.validate(userId, range);
        List<ProblemNoteEntity> records = ActivitySummarySupport.filterValidRecords(
                "problem", listNewProblems(userId, range), ProblemNoteEntity::getTitle);
        Map<Long, String> categoryNames = loadCategoryNames(records, userId);

        ProblemSummary summary = new ProblemSummary();
        summary.setNewCount(records.size());
        summary.setTitles(ActivitySummarySupport.detailTexts(records, ProblemNoteEntity::getTitle));

        List<String> categoryKeys = records.stream()
                .map(item -> normalizeCategoryKey(item.getCategoryId(), categoryNames))
                .toList();
        summary.setCategoryDistribution(ActivitySummarySupport.buildDistribution(
                ActivitySummarySupport.countKeys(categoryKeys),
                key -> resolveCategoryName(key, categoryNames)));

        List<String> difficultyKeys = records.stream()
                .map(item -> normalizeDifficulty(item.getDifficulty()))
                .toList();
        summary.setDifficultyDistribution(ActivitySummarySupport.buildDistribution(
                ActivitySummarySupport.countKeys(difficultyKeys),
                key -> ActivitySummarySupport.UNSET_KEY.equals(key) ? ActivitySummarySupport.UNSET_NAME : key));
        return summary;
    }

    private List<ProblemNoteEntity> listNewProblems(Long userId, AiActivityDateRange range) {
        LambdaQueryWrapper<ProblemNoteEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ProblemNoteEntity::getTitle, ProblemNoteEntity::getCategoryId,
                ProblemNoteEntity::getDifficulty, ProblemNoteEntity::getCreateTime);
        wrapper.eq(ProblemNoteEntity::getUserId, userId);
        wrapper.eq(ProblemNoteEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.ge(ProblemNoteEntity::getCreateTime, range.getStartTime());
        wrapper.lt(ProblemNoteEntity::getCreateTime, range.getEndTime());
        wrapper.orderByDesc(ProblemNoteEntity::getCreateTime);
        return problemNoteMapper.selectList(wrapper);
    }

    private Map<Long, String> loadCategoryNames(List<ProblemNoteEntity> records, Long userId) {
        Set<Long> categoryIds = records.stream()
                .map(ProblemNoteEntity::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<ProblemCategoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ProblemCategoryEntity::getId, ProblemCategoryEntity::getName);
        wrapper.eq(ProblemCategoryEntity::getUserId, userId);
        wrapper.eq(ProblemCategoryEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.in(ProblemCategoryEntity::getId, categoryIds);
        return problemCategoryMapper.selectList(wrapper).stream()
                .filter(item -> item.getId() != null && StringUtils.hasText(item.getName()))
                .collect(Collectors.toMap(ProblemCategoryEntity::getId, item -> item.getName().trim(), (left, right) -> left));
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

    private String normalizeDifficulty(String difficulty) {
        return StringUtils.hasText(difficulty)
                ? difficulty.trim().toLowerCase(Locale.ROOT) : ActivitySummarySupport.UNSET_KEY;
    }
}
