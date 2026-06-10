package top.aiolife.record.service;

import top.aiolife.record.pojo.csdn.CsdnArticleVO;
import top.aiolife.record.pojo.csdn.CsdnStatsVO;

import java.util.List;

/**
 * CSDN 编程看板数据服务。
 *
 * @author Ethan
 * @date 2026-06-08
 */
public interface ICsdnService {

    /**
     * 获取 CSDN 用户主页统计数据。
     *
     * @param username CSDN 用户名
     * @return CSDN 用户统计数据
     *
     * @author Ethan
     * @date 2026-06-08
     */
    CsdnStatsVO getStats(String username);

    /**
     * 获取 CSDN 用户近期文章列表。
     *
     * @param username CSDN 用户名
     * @param limit 最大返回文章数量
     * @return CSDN 近期文章列表
     *
     * @author Ethan
     * @date 2026-06-08
     */
    List<CsdnArticleVO> getArticles(String username, Integer limit);
}
