package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目活动总结统计，承载新增题目及其分类和难度分布。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class ProblemSummary {

    private long newCount;

    private List<String> titles = new ArrayList<>();

    private List<CountItem> categoryDistribution = new ArrayList<>();

    private List<CountItem> difficultyDistribution = new ArrayList<>();

    /**
     * 判断当前题目统计是否没有可输出数据。
     *
     * @return 没有新增题目时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public boolean isEmpty() {
        return newCount == 0;
    }
}
