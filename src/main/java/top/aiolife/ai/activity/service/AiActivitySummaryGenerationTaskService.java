package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivitySummaryGenerationClaim;
import top.aiolife.ai.activity.model.AiActivitySummaryModelResult;
import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;

/**
 * AI 活动总结幂等任务服务，负责任务认领和生成状态持久化。
 *
 * @author Ethan
 * @date 2026-08-15
 */
public interface AiActivitySummaryGenerationTaskService {

    /**
     * 查询指定幂等键对应的生成任务。
     *
     * @param userId 当前用户 ID
     * @param conversationId 会话 ID
     * @param idempotencyKey 幂等键
     * @return 已有任务，不存在时返回 null
     *
     * @author Ethan
     * @date 2026-08-14
     */
    AiActivitySummaryGenerationEntity find(Long userId, Long conversationId, String idempotencyKey);

    /**
     * 创建新任务或按状态认领已有任务。
     *
     * @param userId 当前用户 ID
     * @param conversationId 会话 ID
     * @param idempotencyKey 幂等键
     * @param period 标准统计周期
     * @param userMessage 固定用户指令
     * @param contextJson 生成报告使用的结构化统计快照
     * @return 任务及是否需要调用模型的标识
     *
     * @author Ethan
     * @date 2026-08-15
     */
    AiActivitySummaryGenerationClaim claim(Long userId, Long conversationId, String idempotencyKey,
                                           String period, String userMessage, String contextJson);

    /**
     * 将模型输出保存为可重试落库的 GENERATED 状态。
     *
     * @param taskId 任务 ID
     * @param result 模型生成结果
     * @return 更新后的任务
     *
     * @author Ethan
     * @date 2026-08-14
     */
    AiActivitySummaryGenerationEntity markGenerated(Long taskId, AiActivitySummaryModelResult result);

    /**
     * 将模型生成失败记录为 FAILED 状态。
     *
     * @param taskId 任务 ID
     * @param errorMessage 精简错误信息
     *
     * @author Ethan
     * @date 2026-08-14
     */
    void markFailed(Long taskId, String errorMessage);
}
