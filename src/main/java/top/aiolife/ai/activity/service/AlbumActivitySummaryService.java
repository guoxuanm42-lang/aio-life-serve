package top.aiolife.ai.activity.service;

import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.AlbumSummary;

/**
 * 相册活动统计服务，按统一活动周期汇总用户新建相册文件夹。
 *
 * @author Ethan
 * @date 2026-08-13
 */
public interface AlbumActivitySummaryService {

    /**
     * 汇总指定用户在活动周期内新建的相册文件夹。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 相册活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    AlbumSummary summarize(Long userId, AiActivityDateRange range);
}
