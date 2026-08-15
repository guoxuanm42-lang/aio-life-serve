package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.ActivityItem;
import top.aiolife.ai.activity.pojo.summary.CategoryDuration;
import top.aiolife.ai.activity.pojo.summary.TimeRecordSummary;
import top.aiolife.ai.activity.service.TimeRecordActivitySummaryService;
import top.aiolife.ai.activity.support.AiActivitySummaryPolicy;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.ITimeRecordMapper;
import top.aiolife.record.mapper.ITimeTrackerCategoryMapper;
import top.aiolife.record.pojo.entity.TimeRecordEntity;
import top.aiolife.record.pojo.entity.entity.TimeTrackerCategoryEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 时迹活动统计服务实现，处理业务日期、公共分类覆盖、分类耗时占比和主要活动排序。
 *
 * @author Ethan
 * @date 2026-08-13
 */
@Service
@RequiredArgsConstructor
public class TimeRecordActivitySummaryServiceImpl implements TimeRecordActivitySummaryService {

    private static final Long PUBLIC_CATEGORY_USER_ID = 0L;

    private final ITimeRecordMapper timeRecordMapper;
    private final ITimeTrackerCategoryMapper timeTrackerCategoryMapper;

    /**
     * 汇总指定用户在活动日期范围内的时迹记录。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 时迹活动统计结果
     * @throws IllegalArgumentException 用户、时间或业务日期范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    @Override
    public TimeRecordSummary summarize(Long userId, AiActivityDateRange range) {
        ActivitySummarySupport.validate(userId, range);
        validateDateRange(range);
        List<TimeRecordEntity> records = listRecords(userId, range);
        Map<Long, String> categoryNames = loadCategoryNames(records, userId);

        TimeRecordSummary summary = new TimeRecordSummary();
        summary.setRecordCount(records.size());
        long totalMinutes = records.stream().mapToLong(item -> validDuration(item.getDuration())).sum();
        summary.setTotalMinutes(totalMinutes);
        summary.setCategoryDurations(buildCategoryDurations(records, categoryNames, totalMinutes));
        summary.setMainActivities(buildMainActivities(records, categoryNames));
        return summary;
    }

    private void validateDateRange(AiActivityDateRange range) {
        if (range.getStartDate() == null || range.getEndDate() == null) {
            throw new IllegalArgumentException("时迹统计业务日期范围不能为空");
        }
        if (range.getStartDate().isAfter(range.getEndDate())) {
            throw new IllegalArgumentException("时迹统计开始日期不能晚于结束日期");
        }
    }

    private List<TimeRecordEntity> listRecords(Long userId, AiActivityDateRange range) {
        LambdaQueryWrapper<TimeRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(TimeRecordEntity::getCategoryId, TimeRecordEntity::getDate,
                TimeRecordEntity::getTitle, TimeRecordEntity::getDuration);
        wrapper.eq(TimeRecordEntity::getUserId, userId);
        wrapper.ge(TimeRecordEntity::getDate, range.getStartDate());
        wrapper.le(TimeRecordEntity::getDate, range.getEndDate());
        return timeRecordMapper.selectList(wrapper);
    }

    private Map<Long, String> loadCategoryNames(List<TimeRecordEntity> records, Long userId) {
        Set<Long> categoryIds = records.stream()
                .map(TimeRecordEntity::getCategoryId)
                .map(this::parseCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> names = new HashMap<>();
        listDirectCategories(categoryIds, userId).stream()
                .filter(item -> item.getId() != null && StringUtils.hasText(item.getName()))
                .forEach(item -> names.put(item.getId(), item.getName().trim()));
        listCategoryOverrides(categoryIds, userId).stream()
                .filter(item -> item.getTemplateId() != null && StringUtils.hasText(item.getName()))
                .forEach(item -> names.put(item.getTemplateId(), item.getName().trim()));
        return names;
    }

    private List<TimeTrackerCategoryEntity> listDirectCategories(Set<Long> categoryIds, Long userId) {
        LambdaQueryWrapper<TimeTrackerCategoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(TimeTrackerCategoryEntity::getId, TimeTrackerCategoryEntity::getUserId,
                TimeTrackerCategoryEntity::getTemplateId, TimeTrackerCategoryEntity::getName);
        wrapper.in(TimeTrackerCategoryEntity::getId, categoryIds);
        wrapper.eq(TimeTrackerCategoryEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.and(scope -> scope.eq(TimeTrackerCategoryEntity::getUserId, PUBLIC_CATEGORY_USER_ID)
                .or(privateScope -> privateScope.eq(TimeTrackerCategoryEntity::getUserId, userId)
                        .isNull(TimeTrackerCategoryEntity::getTemplateId)));
        return timeTrackerCategoryMapper.selectList(wrapper);
    }

    private List<TimeTrackerCategoryEntity> listCategoryOverrides(Set<Long> categoryIds, Long userId) {
        LambdaQueryWrapper<TimeTrackerCategoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(TimeTrackerCategoryEntity::getTemplateId, TimeTrackerCategoryEntity::getName);
        wrapper.eq(TimeTrackerCategoryEntity::getUserId, userId);
        wrapper.eq(TimeTrackerCategoryEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.in(TimeTrackerCategoryEntity::getTemplateId, categoryIds);
        return timeTrackerCategoryMapper.selectList(wrapper);
    }

    private List<CategoryDuration> buildCategoryDurations(
            List<TimeRecordEntity> records,
            Map<Long, String> categoryNames,
            long totalMinutes
    ) {
        Map<String, Long> durationMap = new LinkedHashMap<>();
        records.forEach(item -> {
            long duration = validDuration(item.getDuration());
            if (duration > 0) {
                durationMap.merge(resolveCategoryKey(item.getCategoryId(), categoryNames), duration, Long::sum);
            }
        });
        List<CategoryDuration> result = new ArrayList<>();
        durationMap.forEach((key, duration) -> {
            CategoryDuration item = new CategoryDuration();
            item.setCategoryId(key);
            item.setCategoryName(resolveCategoryName(key, categoryNames));
            item.setDurationMinutes(duration);
            item.setPercentage(percentage(duration, totalMinutes));
            result.add(item);
        });
        result.sort(Comparator.comparingLong(CategoryDuration::getDurationMinutes).reversed()
                .thenComparing(CategoryDuration::getCategoryName)
                .thenComparing(CategoryDuration::getCategoryId));
        return result;
    }

    private List<ActivityItem> buildMainActivities(
            List<TimeRecordEntity> records,
            Map<Long, String> categoryNames
    ) {
        return records.stream()
                .filter(item -> StringUtils.hasText(item.getTitle()))
                .filter(item -> validDuration(item.getDuration()) > 0)
                .sorted(Comparator.comparingLong((TimeRecordEntity item) -> validDuration(item.getDuration())).reversed()
                        .thenComparing(TimeRecordEntity::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(item -> item.getTitle().trim()))
                .limit(AiActivitySummaryPolicy.MAIN_ACTIVITY_LIMIT)
                .map(item -> toActivityItem(item, categoryNames))
                .toList();
    }

    private ActivityItem toActivityItem(TimeRecordEntity record, Map<Long, String> categoryNames) {
        ActivityItem item = new ActivityItem();
        item.setTitle(record.getTitle().trim());
        item.setDate(record.getDate());
        item.setDurationMinutes(validDuration(record.getDuration()));
        item.setCategoryName(resolveCategoryName(resolveCategoryKey(record.getCategoryId(), categoryNames), categoryNames));
        return item;
    }

    private long validDuration(Integer duration) {
        return duration != null && duration > 0 ? duration.longValue() : 0L;
    }

    private String resolveCategoryKey(String rawCategoryId, Map<Long, String> categoryNames) {
        Long categoryId = parseCategoryId(rawCategoryId);
        return categoryId != null && categoryNames.containsKey(categoryId)
                ? categoryId.toString() : ActivitySummarySupport.UNCATEGORIZED_KEY;
    }

    private String resolveCategoryName(String key, Map<Long, String> categoryNames) {
        if (ActivitySummarySupport.UNCATEGORIZED_KEY.equals(key)) {
            return ActivitySummarySupport.UNCATEGORIZED_NAME;
        }
        try {
            return categoryNames.getOrDefault(Long.valueOf(key), ActivitySummarySupport.UNCATEGORIZED_NAME);
        } catch (NumberFormatException exception) {
            return ActivitySummarySupport.UNCATEGORIZED_NAME;
        }
    }

    private Long parseCategoryId(String categoryId) {
        if (!StringUtils.hasText(categoryId)) {
            return null;
        }
        try {
            return Long.valueOf(categoryId.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private BigDecimal percentage(long duration, long totalMinutes) {
        if (totalMinutes <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(duration)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalMinutes), 2, RoundingMode.HALF_UP);
    }
}
