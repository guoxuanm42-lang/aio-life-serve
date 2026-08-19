package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.FoodSummary;
import top.aiolife.ai.activity.service.FoodActivitySummaryService;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.IFoodRecordMapper;
import top.aiolife.record.pojo.entity.FoodRecordEntity;

import java.util.List;

/**
 * 美食活动统计服务实现，通过美食记录构建新增数量和菜名明细。
 *
 * @author Ethan
 * @date 2026-08-16
 */
@Service
@RequiredArgsConstructor
public class FoodActivitySummaryServiceImpl implements FoodActivitySummaryService {

    private final IFoodRecordMapper foodRecordMapper;

    /**
     * 汇总指定用户在活动周期内新增的美食记录。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 美食活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-16
     */
    @Override
    public FoodSummary summarize(Long userId, AiActivityDateRange range) {
        ActivitySummarySupport.validate(userId, range);
        LambdaQueryWrapper<FoodRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(FoodRecordEntity::getDishName, FoodRecordEntity::getCreateTime);
        wrapper.eq(FoodRecordEntity::getUserId, userId);
        wrapper.eq(FoodRecordEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.ge(FoodRecordEntity::getCreateTime, range.getStartTime());
        wrapper.lt(FoodRecordEntity::getCreateTime, range.getEndTime());
        wrapper.orderByDesc(FoodRecordEntity::getCreateTime);
        List<FoodRecordEntity> records = ActivitySummarySupport.filterValidRecords(
                "food", foodRecordMapper.selectList(wrapper), FoodRecordEntity::getDishName);

        FoodSummary summary = new FoodSummary();
        summary.setNewCount(records.size());
        summary.setDishNames(ActivitySummarySupport.detailTexts(records, FoodRecordEntity::getDishName));
        return summary;
    }
}
