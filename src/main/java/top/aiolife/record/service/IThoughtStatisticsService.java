package top.aiolife.record.service;

import top.aiolife.record.pojo.vo.ThoughtStatisticsVO;
import top.aiolife.record.pojo.req.ThoughtStatisticsTrendReq;
import top.aiolife.record.pojo.vo.ThoughtStatisticsTrendVO;

/**
 * 闪念统计服务接口。
 *
 * <p>用途：提供当前用户闪念总览指标、状态分布和分类分布统计能力。</p>
 *
 * @author Ethan
 * @date 2026-06-10
 */
public interface IThoughtStatisticsService {

    /**
     * 查询当前用户闪念统计总览。
     *
     * @param userId 当前登录用户 ID
     * @return 闪念统计总览视图
     *
     * @author Ethan
     * @date 2026-06-10
     */
    ThoughtStatisticsVO overview(Long userId);

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
    ThoughtStatisticsTrendVO trend(Long userId, ThoughtStatisticsTrendReq req);
}
