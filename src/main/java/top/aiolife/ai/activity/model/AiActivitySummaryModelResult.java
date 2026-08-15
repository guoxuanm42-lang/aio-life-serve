package top.aiolife.ai.activity.model;

import lombok.Builder;
import lombok.Data;

/**
 * AI 活动总结模型调用结果，封装回复内容和实际运行配置。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Data
@Builder
public class AiActivitySummaryModelResult {
    private String content;
    private String modelName;
    private String agentCode;
    private String agentName;
}
