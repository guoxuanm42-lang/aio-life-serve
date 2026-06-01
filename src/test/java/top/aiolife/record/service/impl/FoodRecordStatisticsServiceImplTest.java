package top.aiolife.record.service.impl;

import org.junit.jupiter.api.Test;
import top.aiolife.record.mapper.IFoodRecordIngredientMapper;
import top.aiolife.record.mapper.IFoodRecordMapper;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.entity.FoodRecordIngredientEntity;
import top.aiolife.record.pojo.vo.FoodRecordStatisticsVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 美食记录统计服务测试。
 *
 * @author Ethan
 * @date 2026-05-31
 */
class FoodRecordStatisticsServiceImplTest {

    private final IFoodRecordMapper foodRecordMapper = mock(IFoodRecordMapper.class);

    private final IFoodRecordIngredientMapper ingredientMapper = mock(IFoodRecordIngredientMapper.class);

    private final FoodRecordStatisticsServiceImpl service =
            new FoodRecordStatisticsServiceImpl(foodRecordMapper, ingredientMapper);

    @Test
    void shouldReturnEmptyStatisticsWhenNoData() {
        when(foodRecordMapper.selectList(any())).thenReturn(List.of());
        when(ingredientMapper.selectList(any())).thenReturn(List.of());

        FoodRecordStatisticsVO result = service.statistics(1L);

        assertEquals(0, result.getOverview().getTotalCount());
        assertEquals(BigDecimal.ZERO, result.getOverview().getAverageRating());
        assertTrue(result.getFrequencyTrend().isEmpty());
        assertTrue(result.getDishRank().isEmpty());
        assertTrue(result.getIngredientRank().isEmpty());
    }

    @Test
    void shouldAggregateOverviewRankAndReminderData() {
        FoodRecordEntity first = record(1L, "番茄炒蛋", "家常菜", "午餐", "done", true);
        first.setRating(BigDecimal.valueOf(8.5));
        first.setTotalMinutes(20);
        first.setCookDate(LocalDate.now());
        first.setSummary("稳定");

        FoodRecordEntity second = record(2L, "番茄炒蛋", "家常菜", "晚餐", "to_improve", false);
        second.setRating(BigDecimal.valueOf(6.5));
        second.setTotalMinutes(30);
        second.setCookDate(LocalDate.now().minusDays(2));
        second.setProblems("偏咸");
        second.setNextImprove("少放盐");

        when(foodRecordMapper.selectList(any())).thenReturn(List.of(first, second));
        when(ingredientMapper.selectList(any())).thenReturn(List.of(
                ingredient("鸡蛋"),
                ingredient("番茄"),
                ingredient("鸡蛋")
        ));

        FoodRecordStatisticsVO result = service.statistics(1L);

        assertEquals(2, result.getOverview().getTotalCount());
        assertEquals(2, result.getOverview().getMonthCount());
        assertEquals(BigDecimal.valueOf(7.5), result.getOverview().getAverageRating());
        assertEquals(BigDecimal.valueOf(25.0).setScale(1), result.getOverview().getAverageTotalMinutes());
        assertEquals(1, result.getOverview().getWorthRedoCount());
        assertEquals(1, result.getOverview().getToImproveCount());
        assertEquals("番茄炒蛋", result.getDishRank().getFirst().getName());
        assertEquals(2, result.getDishRank().getFirst().getCount());
        assertEquals("鸡蛋", result.getIngredientRank().getFirst().getName());
        assertEquals(2, result.getIngredientRank().getFirst().getCount());
        assertEquals(second.getId(), result.getToImproveRecords().getFirst().getId());
        assertEquals(first.getId(), result.getRedoReminders().getFirst().getId());
    }

    private FoodRecordEntity record(Long id,
                                    String dishName,
                                    String category,
                                    String mealType,
                                    String status,
                                    Boolean worthRedo) {
        FoodRecordEntity entity = new FoodRecordEntity();
        entity.setId(id);
        entity.setDishName(dishName);
        entity.setCategory(category);
        entity.setMealType(mealType);
        entity.setStatus(status);
        entity.setWorthRedo(worthRedo);
        return entity;
    }

    private FoodRecordIngredientEntity ingredient(String name) {
        FoodRecordIngredientEntity entity = new FoodRecordIngredientEntity();
        entity.setName(name);
        return entity;
    }
}
