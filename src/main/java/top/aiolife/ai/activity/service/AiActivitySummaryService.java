package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.pojo.req.AiActivitySummaryReq;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;

/**
 * AI 活动统一聚合服务，组织各业务模块统计并返回完整活动上下文。
 *
 * @author Ethan
 * @date 2026-08-13
 */
public interface AiActivitySummaryService {

    /**
     * 聚合指定用户在所选周期内的全部活动统计。
     *
     * @param userId 当前用户 ID
     * @param req AI 活动总结请求
     * @return 统一活动统计上下文
     * @throws IllegalArgumentException 用户、请求或周期参数无效时抛出
     * @throws IllegalStateException 任一模块未返回有效统计对象时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    AiActivitySummaryContext summarize(Long userId, AiActivitySummaryReq req);
}
