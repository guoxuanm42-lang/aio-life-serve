package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.NoteSummary;

/**
 * 笔记活动统计服务，按统一活动周期汇总用户新增笔记。
 *
 * @author Ethan
 * @date 2026-08-13
 */
public interface NoteActivitySummaryService {

    /**
     * 汇总指定用户在活动周期内新增的笔记。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 笔记活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    NoteSummary summarize(Long userId, AiActivityDateRange range);
}
