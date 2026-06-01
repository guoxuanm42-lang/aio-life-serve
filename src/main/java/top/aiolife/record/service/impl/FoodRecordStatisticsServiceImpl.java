package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.record.mapper.IFoodRecordIngredientMapper;
import top.aiolife.record.mapper.IFoodRecordMapper;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.entity.FoodRecordIngredientEntity;
import top.aiolife.record.pojo.vo.FoodRecordStatisticsVO;
import top.aiolife.record.service.IFoodRecordStatisticsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 美食记录统计服务实现，基于现有记录实时聚合总览、趋势、分布和排行。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Service
@RequiredArgsConstructor
public class FoodRecordStatisticsServiceImpl implements IFoodRecordStatisticsService {

    private static final int RANK_LIMIT = 10;

    private static final int SUMMARY_LIMIT = 10;

    private final IFoodRecordMapper foodRecordMapper;

    private final IFoodRecordIngredientMapper ingredientMapper;

    /**
     * 查询当前用户美食记录统计。
     *
     * @param userId 当前用户 ID
     * @return 美食记录统计视图
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Override
    public FoodRecordStatisticsVO statistics(Long userId) {
        List<FoodRecordEntity> records = listRecords(userId);
        List<FoodRecordIngredientEntity> ingredients = listIngredients(userId);

        FoodRecordStatisticsVO vo = new FoodRecordStatisticsVO();
        vo.setOverview(buildOverview(records));
        vo.setFrequencyTrend(buildFrequencyTrend(records));
        vo.setCategoryDistribution(buildDistribution(records, FoodRecordEntity::getCategory));
        vo.setMealTypeDistribution(buildDistribution(records, FoodRecordEntity::getMealType));
        vo.setStatusDistribution(buildDistribution(records, FoodRecordEntity::getStatus));
        vo.setDishRank(buildDishRank(records));
        vo.setIngredientRank(buildIngredientRank(ingredients));
        vo.setToImproveRecords(buildToImproveRecords(records));
        vo.setRedoReminders(buildRedoReminders(records));
        return vo;
    }

    private List<FoodRecordEntity> listRecords(Long userId) {
        LambdaQueryWrapper<FoodRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodRecordEntity::getUserId, userId);
        wrapper.orderByDesc(FoodRecordEntity::getCookDate);
        wrapper.orderByDesc(FoodRecordEntity::getUpdateTime);
        return foodRecordMapper.selectList(wrapper);
    }

    private List<FoodRecordIngredientEntity> listIngredients(Long userId) {
        LambdaQueryWrapper<FoodRecordIngredientEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodRecordIngredientEntity::getUserId, userId);
        return ingredientMapper.selectList(wrapper);
    }

    private FoodRecordStatisticsVO.Overview buildOverview(List<FoodRecordEntity> records) {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        FoodRecordStatisticsVO.Overview overview = new FoodRecordStatisticsVO.Overview();
        overview.setTotalCount(records.size());
        overview.setMonthCount(records.stream()
                .filter(record -> record.getCookDate() != null && !record.getCookDate().isBefore(monthStart))
                .count());
        overview.setAverageRating(average(records.stream()
                .map(FoodRecordEntity::getRating)
                .filter(Objects::nonNull)
                .toList()));
        overview.setAverageTotalMinutes(average(records.stream()
                .map(FoodRecordEntity::getTotalMinutes)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .toList()));
        overview.setWorthRedoCount(records.stream().filter(record -> Boolean.TRUE.equals(record.getWorthRedo())).count());
        overview.setToImproveCount(records.stream().filter(this::isToImprove).count());
        return overview;
    }

    private List<FoodRecordStatisticsVO.TrendItem> buildFrequencyTrend(List<FoodRecordEntity> records) {
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> countMap = records.stream()
                .filter(record -> record.getCookDate() != null)
                .collect(Collectors.groupingBy(record -> {
                    LocalDate date = record.getCookDate();
                    int week = date.get(weekFields.weekOfWeekBasedYear());
                    return date.format(monthFormatter) + " 第" + week + "周";
                }, Collectors.counting()));
        return countMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    FoodRecordStatisticsVO.TrendItem item = new FoodRecordStatisticsVO.TrendItem();
                    item.setLabel(entry.getKey());
                    item.setCount(entry.getValue());
                    return item;
                })
                .toList();
    }

    private List<FoodRecordStatisticsVO.DistributionItem> buildDistribution(List<FoodRecordEntity> records,
                                                                            Function<FoodRecordEntity, String> getter) {
        return records.stream()
                .map(getter)
                .map(this::normalizeName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(countDescThenName())
                .map(entry -> {
                    FoodRecordStatisticsVO.DistributionItem item = new FoodRecordStatisticsVO.DistributionItem();
                    item.setName(entry.getKey());
                    item.setCount(entry.getValue());
                    return item;
                })
                .toList();
    }

    private List<FoodRecordStatisticsVO.RankItem> buildDishRank(List<FoodRecordEntity> records) {
        return records.stream()
                .map(FoodRecordEntity::getDishName)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(countDescThenName())
                .limit(RANK_LIMIT)
                .map(this::toRankItem)
                .toList();
    }

    private List<FoodRecordStatisticsVO.RankItem> buildIngredientRank(List<FoodRecordIngredientEntity> ingredients) {
        return ingredients.stream()
                .map(FoodRecordIngredientEntity::getName)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(countDescThenName())
                .limit(RANK_LIMIT)
                .map(this::toRankItem)
                .toList();
    }

    private List<FoodRecordStatisticsVO.RecordSummary> buildToImproveRecords(List<FoodRecordEntity> records) {
        return records.stream()
                .filter(this::isToImprove)
                .sorted(recordSummaryComparator())
                .limit(SUMMARY_LIMIT)
                .map(this::toSummary)
                .toList();
    }

    private List<FoodRecordStatisticsVO.RecordSummary> buildRedoReminders(List<FoodRecordEntity> records) {
        return records.stream()
                .filter(record -> Boolean.TRUE.equals(record.getWorthRedo()))
                .sorted(recordSummaryComparator())
                .limit(SUMMARY_LIMIT)
                .map(this::toSummary)
                .toList();
    }

    private FoodRecordStatisticsVO.RecordSummary toSummary(FoodRecordEntity record) {
        FoodRecordStatisticsVO.RecordSummary summary = new FoodRecordStatisticsVO.RecordSummary();
        summary.setId(record.getId());
        summary.setDishName(record.getDishName());
        summary.setCookDate(record.getCookDate());
        summary.setCategory(record.getCategory());
        summary.setMealType(record.getMealType());
        summary.setStatus(record.getStatus());
        summary.setRating(record.getRating());
        summary.setTotalMinutes(record.getTotalMinutes());
        summary.setProblems(record.getProblems());
        summary.setNextImprove(record.getNextImprove());
        summary.setSummary(record.getSummary());
        summary.setWorthRedo(record.getWorthRedo());
        return summary;
    }

    private FoodRecordStatisticsVO.RankItem toRankItem(Map.Entry<String, Long> entry) {
        FoodRecordStatisticsVO.RankItem item = new FoodRecordStatisticsVO.RankItem();
        item.setName(entry.getKey());
        item.setCount(entry.getValue());
        return item;
    }

    private boolean isToImprove(FoodRecordEntity record) {
        return "to_improve".equals(record.getStatus())
                || StringUtils.hasText(record.getProblems())
                || StringUtils.hasText(record.getNextImprove());
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 1, RoundingMode.HALF_UP);
    }

    private String normalizeName(String value) {
        return StringUtils.hasText(value) ? value.trim() : "未分类";
    }

    private <T extends Map.Entry<String, Long>> Comparator<T> countDescThenName() {
        return Comparator.<T, Long>comparing(Map.Entry::getValue).reversed()
                .thenComparing(Map.Entry::getKey);
    }

    private Comparator<FoodRecordEntity> recordSummaryComparator() {
        return Comparator.comparing(
                FoodRecordEntity::getCookDate,
                Comparator.nullsLast(Comparator.reverseOrder())
        ).thenComparing(
                FoodRecordEntity::getUpdateTime,
                Comparator.nullsLast(Comparator.reverseOrder())
        );
    }
}
