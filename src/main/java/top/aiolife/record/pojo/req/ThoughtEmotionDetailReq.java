package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 闪念情绪型详情保存请求。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Data
public class ThoughtEmotionDetailReq {

    private String emotionType;

    private Integer emotionIntensity;

    private String emotionTrigger;

    private String emotionNeed;

    private String copingAction;

    private String reflectionSummary;

    private String ignoredReason;
}
