package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 美食活动总结统计，承载新增记录数量和菜名。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class FoodSummary {

    private long newCount;

    private List<String> dishNames = new ArrayList<>();

    /**
     * 判断当前美食统计是否没有可输出数据。
     *
     * @return 没有新增美食记录时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public boolean isEmpty() {
        return newCount == 0;
    }
}
