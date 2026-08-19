package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivitySummaryProgressStage;

/**
 * 活动总结生成进度监听器，用于在不改变生成逻辑的前提下接收真实阶段事件。
 *
 * @author Ethan
 * @date 2026-08-16
 */
@FunctionalInterface
public interface AiActivitySummaryProgressListener {

    AiActivitySummaryProgressListener NONE = stage -> {
    };

    /**
     * 接收活动总结生成阶段变化。
     *
     * @param stage 当前真实生成阶段
     *
     * @author Ethan
     * @date 2026-08-16
     */
    void onProgress(AiActivitySummaryProgressStage stage);
}
