package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.time.LocalDate;

/**
 * 时迹主要活动摘要项，用于向后续 AI 总结提供有限的活动信息。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class ActivityItem {

    private String title;

    private LocalDate date;

    private long durationMinutes;

    private String categoryName;
}
