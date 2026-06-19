package top.aiolife.record.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 闪念情绪型详情保存请求。
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtEmotionDetailReq {

    @McpField(description = "情绪类型：sad/angry/anxious/stress/happy/excited/moved/inspired")
    private String emotionType;

    @McpField(description = "情绪强度：1-5")
    private Integer emotionIntensity;

    @McpField(description = "触发原因：发生了什么")
    private String emotionTrigger;

    @McpField(description = "背后需求：这条情绪背后的需要")
    private String emotionNeed;

    @McpField(description = "缓解动作：做了什么让情绪缓解")
    private String copingAction;

    @McpField(description = "复盘结论：从这次情绪中得到的结论")
    private String reflectionSummary;

    @McpField(description = "不再关注原因")
    private String ignoredReason;
}
