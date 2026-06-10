package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.pojo.entity.ThoughtEntity;
import top.aiolife.record.pojo.req.ThoughtStatisticsTrendReq;
import top.aiolife.record.pojo.vo.ThoughtStatisticsTrendVO;
import top.aiolife.record.pojo.vo.ThoughtStatisticsVO;
import top.aiolife.record.service.IThoughtStatisticsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 闪念统计服务实现。
 *
 * <p>用途：基于 thought 表现有数据实时聚合当前用户闪念总览、状态分布和分类分布。</p>
 *
 * @author Ethan
 * @date 2026-06-10
 */
@Service
@RequiredArgsConstructor
public class ThoughtStatisticsServiceImpl implements IThoughtStatisticsService {

    private static final String STATUS_PENDING = "pending";

    private static final String STATUS_ONGOING = "ongoing";

    private static final String STATUS_DONE = "done";

    private static final String STATUS_SHELVED = "shelved";

    private static final String STATUS_ARCHIVED = "archived";

    private static final String UNKNOWN_CATEGORY_KEY = "unknown";

    private static final Set<String> SUPPORTED_RANGES = Set.of("7d", "30d", "month", "year");

    private static final Set<String> SUPPORTED_GROUPS = Set.of("day", "week", "month");

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final IThoughtMapper thoughtMapper;

    /**
     * 查询当前用户闪念统计总览。
     *
     * @param userId 当前登录用户 ID
     * @return 闪念统计总览视图
     *
     * @author Ethan
     * @date 2026-06-10
     */
    @Override
    public ThoughtStatisticsVO overview(Long userId) {
        List<ThoughtEntity> records = listRecords(userId);
        ThoughtStatisticsVO vo = new ThoughtStatisticsVO();
        vo.setSummary(buildSummary(records));
        vo.setStatusDistribution(buildStatusDistribution(records));
        vo.setCategoryDistribution(buildCategoryDistribution(records));
        return vo;
    }

    /**
     * 查询当前用户闪念时间趋势统计。
     *
     * @param userId 当前登录用户 ID
     * @param req 趋势统计查询请求
     * @return 闪念趋势统计视图
     *
     * @author Ethan
     * @date 2026-06-10
     */
    @Override
    public ThoughtStatisticsTrendVO trend(Long userId, ThoughtStatisticsTrendReq req) {
        ThoughtStatisticsTrendReq safeReq = req == null ? new ThoughtStatisticsTrendReq() : req;
        String range = normalizeRange(safeReq.getRange());
        String groupBy = normalizeGroupBy(safeReq.getGroupBy());
        DateRange dateRange = resolveDateRange(range);
        Map<String, String> categoryNames = buildCategoryNames();

        List<ThoughtEntity> filteredRecords = listRecords(userId).stream()
                .filter(record -> isInDateRange(record.getCreateTime(), dateRange))
                .filter(record -> matchesCategory(record, safeReq.getCategory(), categoryNames))
                .filter(record -> matchesStatus(record, safeReq.getStatus()))
                .toList();

        ThoughtStatisticsTrendVO vo = new ThoughtStatisticsTrendVO();
        vo.setRange(range);
        vo.setGroupBy(groupBy);
        vo.setTrend(buildTrendPoints(filteredRecords, dateRange.startDate(), dateRange.endDate(), groupBy));
        vo.setCategoryTrends(buildCategoryTrends(filteredRecords, dateRange.startDate(), dateRange.endDate(), groupBy, categoryNames, safeReq.getCategory()));
        vo.setActivity(buildTrendPoints(filteredRecords, dateRange.startDate(), dateRange.endDate(), "day"));
        vo.setBurstDays(buildBurstDays(vo.getActivity()));
        return vo;
    }

    private List<ThoughtEntity> listRecords(Long userId) {
        LambdaQueryWrapper<ThoughtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtEntity::getUserId, userId);
        wrapper.eq(ThoughtEntity::getIsDeleted, 0);
        return thoughtMapper.selectList(wrapper);
    }

    private ThoughtStatisticsVO.Summary buildSummary(List<ThoughtEntity> records) {
        LocalDateTime weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long totalCount = records.size();
        long pendingCount = countStatus(records, STATUS_PENDING);
        long ongoingCount = countStatus(records, STATUS_ONGOING);
        long doneCount = countStatus(records, STATUS_DONE);
        long archivedCount = countStatus(records, STATUS_ARCHIVED);

        ThoughtStatisticsVO.Summary summary = new ThoughtStatisticsVO.Summary();
        summary.setTotalCount(totalCount);
        summary.setWeekNewCount(records.stream().filter(record -> isAfterOrEqual(record.getCreateTime(), weekStart)).count());
        summary.setMonthNewCount(records.stream().filter(record -> isAfterOrEqual(record.getCreateTime(), monthStart)).count());
        summary.setPendingCount(pendingCount);
        summary.setDoneCount(doneCount);
        summary.setArchivedCount(archivedCount);
        summary.setConversionRate(percent(doneCount, totalCount));
        summary.setBacklogCount(pendingCount + ongoingCount);
        summary.setHighValueCount(archivedCount);
        return summary;
    }

    private List<ThoughtStatisticsVO.DistributionItem> buildStatusDistribution(List<ThoughtEntity> records) {
        Map<String, String> statusNames = new LinkedHashMap<>();
        statusNames.put(STATUS_PENDING, "待处理");
        statusNames.put(STATUS_ONGOING, "进行中");
        statusNames.put(STATUS_DONE, "已完成");
        statusNames.put(STATUS_SHELVED, "已搁置");
        statusNames.put(STATUS_ARCHIVED, "已归档");

        Map<String, Long> countMap = records.stream()
                .map(record -> normalizeStatus(record.getStatus()))
                .filter(statusNames::containsKey)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return statusNames.entrySet().stream()
                .map(entry -> toDistributionItem(entry.getKey(), entry.getValue(), countMap.getOrDefault(entry.getKey(), 0L), records.size()))
                .toList();
    }

    private List<ThoughtStatisticsVO.DistributionItem> buildCategoryDistribution(List<ThoughtEntity> records) {
        Map<String, String> categoryNames = buildCategoryNames();

        Map<String, Long> countMap = records.stream()
                .map(record -> normalizeCategory(record.getThemeKey(), categoryNames))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<ThoughtStatisticsVO.DistributionItem> result = categoryNames.entrySet().stream()
                .map(entry -> toDistributionItem(entry.getKey(), entry.getValue(), countMap.getOrDefault(entry.getKey(), 0L), records.size()))
                .collect(Collectors.toList());
        long unknownCount = countMap.getOrDefault(UNKNOWN_CATEGORY_KEY, 0L);
        if (unknownCount > 0) {
            result.add(toDistributionItem(UNKNOWN_CATEGORY_KEY, "未分类", unknownCount, records.size()));
        }
        return result;
    }

    private Map<String, String> buildCategoryNames() {
        Map<String, String> categoryNames = new LinkedHashMap<>();
        categoryNames.put("cyan", "工作");
        categoryNames.put("green", "生活");
        categoryNames.put("teal", "健康");
        categoryNames.put("blue", "学习");
        categoryNames.put("pink", "社交");
        categoryNames.put("purple", "创作");
        categoryNames.put("indigo", "AIO-LIFE开发");
        categoryNames.put("orange", "旅行");
        return categoryNames;
    }

    private List<ThoughtStatisticsTrendVO.Point> buildTrendPoints(List<ThoughtEntity> records, LocalDate startDate, LocalDate endDate, String groupBy) {
        Map<String, Long> countMap = records.stream()
                .filter(record -> record.getCreateTime() != null)
                .collect(Collectors.groupingBy(record -> bucketKey(record.getCreateTime().toLocalDate(), groupBy), Collectors.counting()));

        return buildBucketKeys(startDate, endDate, groupBy).stream()
                .map(key -> toTrendPoint(key, countMap.getOrDefault(key, 0L)))
                .toList();
    }

    private List<ThoughtStatisticsTrendVO.CategoryTrend> buildCategoryTrends(
            List<ThoughtEntity> records,
            LocalDate startDate,
            LocalDate endDate,
            String groupBy,
            Map<String, String> categoryNames,
            String selectedCategory) {
        String normalizedSelectedCategory = normalizeCategoryFilter(selectedCategory, categoryNames);
        Map<String, List<ThoughtEntity>> groupedRecords = records.stream()
                .collect(Collectors.groupingBy(record -> normalizeCategory(record.getThemeKey(), categoryNames)));

        List<String> categoryKeys = new ArrayList<>(categoryNames.keySet());
        if (groupedRecords.containsKey(UNKNOWN_CATEGORY_KEY)) {
            categoryKeys.add(UNKNOWN_CATEGORY_KEY);
        }
        if (StringUtils.hasText(normalizedSelectedCategory)) {
            categoryKeys = categoryKeys.stream()
                    .filter(key -> Objects.equals(key, normalizedSelectedCategory))
                    .toList();
        }

        return categoryKeys.stream()
                .map(key -> toCategoryTrend(key, categoryName(key, categoryNames), groupedRecords.getOrDefault(key, List.of()), startDate, endDate, groupBy))
                .filter(item -> item.getPoints().stream().anyMatch(point -> point.getCount() > 0))
                .toList();
    }

    private ThoughtStatisticsTrendVO.CategoryTrend toCategoryTrend(
            String categoryKey,
            String categoryName,
            List<ThoughtEntity> records,
            LocalDate startDate,
            LocalDate endDate,
            String groupBy) {
        ThoughtStatisticsTrendVO.CategoryTrend trend = new ThoughtStatisticsTrendVO.CategoryTrend();
        trend.setCategoryKey(categoryKey);
        trend.setCategoryName(categoryName);
        trend.setPoints(buildTrendPoints(records, startDate, endDate, groupBy));
        return trend;
    }

    private List<ThoughtStatisticsTrendVO.BurstDay> buildBurstDays(List<ThoughtStatisticsTrendVO.Point> activity) {
        if (activity.isEmpty()) {
            return List.of();
        }
        long total = activity.stream().mapToLong(ThoughtStatisticsTrendVO.Point::getCount).sum();
        BigDecimal average = BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(activity.size()), 1, RoundingMode.HALF_UP);
        BigDecimal threshold = average.multiply(BigDecimal.valueOf(2));
        BigDecimal minThreshold = BigDecimal.valueOf(3);
        BigDecimal finalThreshold = threshold.compareTo(minThreshold) > 0 ? threshold : minThreshold;

        return activity.stream()
                .filter(point -> BigDecimal.valueOf(point.getCount()).compareTo(finalThreshold) >= 0)
                .sorted(Comparator.comparing(ThoughtStatisticsTrendVO.Point::getDate))
                .map(point -> toBurstDay(point, average))
                .toList();
    }

    private ThoughtStatisticsTrendVO.BurstDay toBurstDay(ThoughtStatisticsTrendVO.Point point, BigDecimal average) {
        ThoughtStatisticsTrendVO.BurstDay burstDay = new ThoughtStatisticsTrendVO.BurstDay();
        burstDay.setDate(point.getDate());
        burstDay.setCount(point.getCount());
        burstDay.setAverage(average);
        return burstDay;
    }

    private ThoughtStatisticsTrendVO.Point toTrendPoint(String date, long count) {
        ThoughtStatisticsTrendVO.Point point = new ThoughtStatisticsTrendVO.Point();
        point.setDate(date);
        point.setCount(count);
        return point;
    }

    private List<String> buildBucketKeys(LocalDate startDate, LocalDate endDate, String groupBy) {
        List<String> keys = new ArrayList<>();
        LocalDate cursor = bucketDate(startDate, groupBy);
        LocalDate endBucket = bucketDate(endDate, groupBy);
        while (!cursor.isAfter(endBucket)) {
            keys.add(formatBucket(cursor, groupBy));
            cursor = switch (groupBy) {
                case "week" -> cursor.plusWeeks(1);
                case "month" -> cursor.plusMonths(1);
                default -> cursor.plusDays(1);
            };
        }
        return keys;
    }

    private String bucketKey(LocalDate date, String groupBy) {
        return formatBucket(bucketDate(date, groupBy), groupBy);
    }

    private LocalDate bucketDate(LocalDate date, String groupBy) {
        return switch (groupBy) {
            case "week" -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case "month" -> date.withDayOfMonth(1);
            default -> date;
        };
    }

    private String formatBucket(LocalDate date, String groupBy) {
        if (Objects.equals(groupBy, "month")) {
            return date.format(MONTH_FORMATTER);
        }
        return date.format(DAY_FORMATTER);
    }

    private DateRange resolveDateRange(String range) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = switch (range) {
            case "7d" -> today.minusDays(6);
            case "month" -> today.withDayOfMonth(1);
            case "year" -> today.withDayOfYear(1);
            default -> today.minusDays(29);
        };
        return new DateRange(startDate, today);
    }

    private boolean isInDateRange(LocalDateTime createTime, DateRange dateRange) {
        if (createTime == null) {
            return false;
        }
        LocalDate date = createTime.toLocalDate();
        return !date.isBefore(dateRange.startDate()) && !date.isAfter(dateRange.endDate());
    }

    private boolean matchesCategory(ThoughtEntity record, String category, Map<String, String> categoryNames) {
        String normalizedCategory = normalizeCategoryFilter(category, categoryNames);
        if (!StringUtils.hasText(normalizedCategory)) {
            return true;
        }
        return Objects.equals(normalizeCategory(record.getThemeKey(), categoryNames), normalizedCategory);
    }

    private boolean matchesStatus(ThoughtEntity record, String status) {
        String normalizedStatus = normalizeStatusFilter(status);
        if (!StringUtils.hasText(normalizedStatus)) {
            return true;
        }
        return Objects.equals(normalizeStatus(record.getStatus()), normalizedStatus);
    }

    private String normalizeRange(String range) {
        String normalized = StringUtils.hasText(range) ? range.trim().toLowerCase() : "30d";
        return SUPPORTED_RANGES.contains(normalized) ? normalized : "30d";
    }

    private String normalizeGroupBy(String groupBy) {
        String normalized = StringUtils.hasText(groupBy) ? groupBy.trim().toLowerCase() : "day";
        return SUPPORTED_GROUPS.contains(normalized) ? normalized : "day";
    }

    private String normalizeCategoryFilter(String category, Map<String, String> categoryNames) {
        if (!StringUtils.hasText(category)) {
            return "";
        }
        String normalized = category.trim().toLowerCase();
        if (UNKNOWN_CATEGORY_KEY.equals(normalized)) {
            return UNKNOWN_CATEGORY_KEY;
        }
        return categoryNames.containsKey(normalized) ? normalized : "";
    }

    private String normalizeStatusFilter(String status) {
        String normalized = normalizeStatus(status);
        if (Objects.equals(normalized, STATUS_PENDING)
                || Objects.equals(normalized, STATUS_ONGOING)
                || Objects.equals(normalized, STATUS_DONE)
                || Objects.equals(normalized, STATUS_SHELVED)
                || Objects.equals(normalized, STATUS_ARCHIVED)) {
            return normalized;
        }
        return "";
    }

    private String categoryName(String key, Map<String, String> categoryNames) {
        return UNKNOWN_CATEGORY_KEY.equals(key) ? "未分类" : categoryNames.getOrDefault(key, "未分类");
    }

    private long countStatus(List<ThoughtEntity> records, String status) {
        return records.stream()
                .filter(record -> Objects.equals(normalizeStatus(record.getStatus()), status))
                .count();
    }

    private boolean isAfterOrEqual(LocalDateTime value, LocalDateTime boundary) {
        return value != null && !value.isBefore(boundary);
    }

    private String normalizeStatus(String status) {
        return StringUtils.hasText(status) ? status.trim().toLowerCase() : "";
    }

    private String normalizeCategory(String themeKey, Map<String, String> categoryNames) {
        if (!StringUtils.hasText(themeKey)) {
            return UNKNOWN_CATEGORY_KEY;
        }
        String normalized = themeKey.trim().toLowerCase();
        return categoryNames.containsKey(normalized) ? normalized : UNKNOWN_CATEGORY_KEY;
    }

    private ThoughtStatisticsVO.DistributionItem toDistributionItem(String key, String name, long count, long total) {
        ThoughtStatisticsVO.DistributionItem item = new ThoughtStatisticsVO.DistributionItem();
        item.setKey(key);
        item.setName(name);
        item.setCount(count);
        item.setPercent(percent(count, total));
        return item;
    }

    private BigDecimal percent(long count, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(count)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
