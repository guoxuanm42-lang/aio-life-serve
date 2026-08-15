package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivitySummaryModelResult;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;

/**
 * AI 活动总结专用模型服务，以无记忆、无工具方式生成统计复盘。
 *
 * @author Ethan
 * @date 2026-08-14
 */
public interface AiActivitySummaryModelService {

    /**
     * 使用指定 Agent 对受控活动统计生成自然语言总结。
     *
     * @param userId 当前用户 ID
     * @param agentCode 会话绑定的 Agent 编码
     * @param context 活动统计上下文
     * @param userMessage 固定用户总结指令
     * @return 模型回复及实际 Agent、模型信息
     *
     * @author Ethan
     * @date 2026-08-14
     */
    AiActivitySummaryModelResult generate(Long userId, String agentCode,
                                          AiActivitySummaryContext context, String userMessage);
}
