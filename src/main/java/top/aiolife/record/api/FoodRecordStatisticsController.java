package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.record.pojo.vo.FoodRecordStatisticsVO;
import top.aiolife.record.service.IFoodRecordStatisticsService;

/**
 * 美食记录统计控制器，提供当前用户的做饭统计、排行和提醒接口。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/food-record/statistics")
public class FoodRecordStatisticsController {

    private final IFoodRecordStatisticsService foodRecordStatisticsService;

    /**
     * 查询当前用户美食记录统计。
     *
     * <p>用途：前端统计视图获取总览、趋势、分布、排行、待优化和复做提醒数据。</p>
     *
     * @return 统一返回结构，data 为美食记录统计视图
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @GetMapping
    public ApiResponse<FoodRecordStatisticsVO> statistics() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(foodRecordStatisticsService.statistics(userId));
    }
}
