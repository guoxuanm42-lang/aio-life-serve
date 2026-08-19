package top.aiolife.ai.activity.model;

import lombok.Getter;

/**
 * 活动总结生成进度阶段，描述报告生成链路中的真实业务边界。
 *
 * @author Ethan
 * @date 2026-08-16
 */
@Getter
public enum AiActivitySummaryProgressStage {

    COLLECTING("正在汇总活动数据", 15),
    PREPARING("正在整理报告上下文", 40),
    GENERATING("AI 正在生成复盘", 55),
    SAVING("正在保存报告", 90),
    COMPLETED("报告已生成", 100);

    private final String label;
    private final int percent;

    AiActivitySummaryProgressStage(String label, int percent) {
        this.label = label;
        this.percent = percent;
    }
}
