package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章活动总结统计，区分周期内新增文章和既有文章更新。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class ArticleSummary {

    private long newCount;

    private long updatedCount;

    private List<String> newTitles = new ArrayList<>();

    private List<String> updatedTitles = new ArrayList<>();

    private List<CountItem> categoryDistribution = new ArrayList<>();

    /**
     * 判断当前文章统计是否没有可输出数据。
     *
     * @return 没有新增或更新文章时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public boolean isEmpty() {
        return newCount == 0 && updatedCount == 0;
    }
}
