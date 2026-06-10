package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.record.pojo.req.ThoughtStatisticsTrendReq;
import top.aiolife.record.pojo.vo.ThoughtStatisticsTrendVO;
import top.aiolife.record.pojo.vo.ThoughtStatisticsVO;
import top.aiolife.record.service.IThoughtStatisticsService;

/**
 * 闪念统计控制器。
 *
 * <p>用途：为前端闪念统计洞察页提供总览指标、状态分布和分类分布数据。</p>
 *
 * @author Ethan
 * @date 2026-06-10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/thought/statistics")
public class ThoughtStatisticsController {

    private final IThoughtStatisticsService thoughtStatisticsService;

    /**
     * 查询当前用户闪念统计总览接口。
     *
     * <p>用途：前端统计洞察页获取总闪念数、新增数、状态指标、状态分布和分类分布。</p>
     *
     * @return 统一返回结构，data 为闪念统计总览视图
     *
     * @author Ethan
     * @date 2026-06-10
     */
    @GetMapping("/overview")
    public ApiResponse<ThoughtStatisticsVO> overview() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(thoughtStatisticsService.overview(userId));
    }

    /**
     * 查询当前用户闪念时间趋势接口。
     *
     * <p>用途：前端统计洞察页获取新增趋势、分类趋势、活跃度和爆发日数据。</p>
     *
     * @param req 趋势统计查询参数（包含时间范围、分组方式、分类和状态筛选）
     * @return 统一返回结构，data 为闪念趋势统计视图
     *
     * @author Ethan
     * @date 2026-06-10
     */
    @GetMapping("/trend")
    public ApiResponse<ThoughtStatisticsTrendVO> trend(ThoughtStatisticsTrendReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(thoughtStatisticsService.trend(userId, req));
    }
}
