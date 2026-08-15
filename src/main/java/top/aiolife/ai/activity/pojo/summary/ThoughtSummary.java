package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 闪念活动总结统计，承载新增数量、类型主题分布和新增标题。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class ThoughtSummary {

    private long newCount;

    private List<CountItem> typeDistribution = new ArrayList<>();

    private List<CountItem> themeDistribution = new ArrayList<>();

    private List<String> titles = new ArrayList<>();

    /**
     * 判断当前闪念统计是否没有可输出数据。
     *
     * @return 没有新增闪念时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public boolean isEmpty() {
        return newCount == 0;
    }
}
