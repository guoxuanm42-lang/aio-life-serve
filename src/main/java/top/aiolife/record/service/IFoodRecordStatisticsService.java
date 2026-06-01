package top.aiolife.record.service;

import top.aiolife.record.pojo.vo.FoodRecordStatisticsVO;

/**
 * 美食记录统计服务接口，提供当前用户的轻量统计和提醒数据。
 *
 * @author Ethan
 * @date 2026-05-31
 */
public interface IFoodRecordStatisticsService {

    /**
     * 查询当前用户美食记录统计。
     *
     * @param userId 当前用户 ID
     * @return 美食记录统计视图
     *
     * @author Ethan
     * @date 2026-05-31
     */
    FoodRecordStatisticsVO statistics(Long userId);
}
