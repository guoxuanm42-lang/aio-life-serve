package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 时迹活动总结统计，承载记录数量、总时长、分类耗时和主要活动。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class TimeRecordSummary {

    private long recordCount;

    private long totalMinutes;

    private List<CategoryDuration> categoryDurations = new ArrayList<>();

    private List<ActivityItem> mainActivities = new ArrayList<>();

    /**
     * 判断当前时迹统计是否没有可输出数据。
     *
     * @return 没有时迹记录时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public boolean isEmpty() {
        return recordCount == 0;
    }
}
