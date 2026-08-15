package top.aiolife.ai.activity.pojo.req;

import lombok.Data;

/**
 * AI 活动总结请求，指定统计周期和后续承载总结的会话。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class AiActivitySummaryReq {

    /**
     * 统计周期，仅支持 week、month、year。
     */
    private String period;

    /**
     * 会话 id，可为空，预留给后续 AI 总结阶段使用。
     */
    private Long conversationId;
}
