package top.aiolife.ai.activity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;

/**
 * AI 活动总结幂等任务认领结果，标识当前请求是否负责调用模型。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Data
@AllArgsConstructor
public class AiActivitySummaryGenerationClaim {
    private AiActivitySummaryGenerationEntity task;
    private boolean modelGenerationRequired;
}
