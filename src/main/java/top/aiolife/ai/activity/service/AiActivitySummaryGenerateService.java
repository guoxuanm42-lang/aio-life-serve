package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.pojo.req.AiActivitySummaryGenerateReq;
import top.aiolife.ai.activity.pojo.resp.AiActivitySummaryGenerateResp;

/**
 * AI 活动总结生成服务，编排会话校验、统计聚合、模型生成和消息持久化。
 *
 * @author Ethan
 * @date 2026-08-14
 */
public interface AiActivitySummaryGenerateService {

    /**
     * 为当前用户会话生成并保存指定周期的活动总结。
     *
     * @param userId 当前用户 ID
     * @param req 活动总结生成请求
     * @return 生成内容及落库消息信息
     *
     * @author Ethan
     * @date 2026-08-14
     */
    AiActivitySummaryGenerateResp generate(Long userId, AiActivitySummaryGenerateReq req);
}
