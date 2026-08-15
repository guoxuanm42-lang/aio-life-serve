package top.aiolife.ai.activity.pojo.req;

import lombok.Data;

/**
 * AI 活动总结生成请求，指定统计周期、目标会话和请求幂等键。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Data
public class AiActivitySummaryGenerateReq {
    private String period;
    private Long conversationId;
    private String idempotencyKey;
}
