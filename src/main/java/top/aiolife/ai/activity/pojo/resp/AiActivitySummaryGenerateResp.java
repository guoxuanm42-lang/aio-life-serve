package top.aiolife.ai.activity.pojo.resp;

import lombok.Builder;
import lombok.Data;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;

/**
 * AI 活动总结生成响应，返回已持久化消息及实际 Agent、模型信息。
 *
 * @author Ethan
 * @date 2026-08-15
 */
@Data
@Builder
public class AiActivitySummaryGenerateResp {
    private Long conversationId;
    private Long userMessageId;
    private Long assistantMessageId;
    private String period;
    private String userMessage;
    private String content;
    private AiActivitySummaryContext activitySummary;
    private String modelName;
    private String agentCode;
    private String agentName;
}
