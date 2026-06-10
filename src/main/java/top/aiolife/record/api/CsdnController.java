package top.aiolife.record.api;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.record.pojo.csdn.CsdnArticleVO;
import top.aiolife.record.pojo.csdn.CsdnStatsVO;
import top.aiolife.record.service.ICsdnService;

import java.util.List;

/**
 * CSDN 编程看板接口。
 *
 * @author Ethan
 * @date 2026-06-08
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/csdn")
public class CsdnController {

    private final ICsdnService csdnService;

    /**
     * 获取 CSDN 用户统计接口。
     *
     * <p>用途：前端根据当前用户绑定的 CSDN 用户名查询主页统计数据，返回编程看板统计卡片所需结构。</p>
     *
     * @param username CSDN 用户名，用于拼接 CSDN 博客主页地址
     * @return 统一返回结构，data 为 CSDN 用户访问量、原创数、排名、粉丝数、获赞数和评论数
     *
     * @author Ethan
     * @date 2026-06-08
     */
    @SaCheckLogin
    @GetMapping("/stats")
    public ApiResponse<CsdnStatsVO> getStats(@RequestParam String username) {
        return ApiResponse.success(csdnService.getStats(username));
    }

    /**
     * 获取 CSDN 近期文章接口。
     *
     * <p>用途：前端根据当前用户绑定的 CSDN 用户名查询近期文章列表，返回文章标题、摘要、发布时间和互动数据。</p>
     *
     * @param username CSDN 用户名，用于拼接 CSDN 博客主页地址
     * @param limit 最大返回文章数量，后端会限制在 1 到 50 之间
     * @return 统一返回结构，data 为 CSDN 近期文章列表
     *
     * @author Ethan
     * @date 2026-06-08
     */
    @SaCheckLogin
    @GetMapping("/articles")
    public ApiResponse<List<CsdnArticleVO>> getArticles(
            @RequestParam String username,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        return ApiResponse.success(csdnService.getArticles(username, limit));
    }
}
