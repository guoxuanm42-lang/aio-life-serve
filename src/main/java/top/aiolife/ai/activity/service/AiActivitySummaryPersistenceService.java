package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;
import top.aiolife.ai.activity.pojo.resp.AiActivitySummaryGenerateResp;

/**
 * AI 活动总结消息持久化服务，原子保存双边消息、任务成功状态和会话活动时间。
 *
 * @author Ethan
 * @date 2026-08-14
 */
public interface AiActivitySummaryPersistenceService {

    /**
     * 将 GENERATED 任务原子落为用户消息和助手消息。
     *
     * @param task 已完成模型生成的幂等任务
     * @return 与数据库消息完全一致的生成响应
     *
     * @author Ethan
     * @date 2026-08-14
     */
    AiActivitySummaryGenerateResp persist(AiActivitySummaryGenerationEntity task);
}
