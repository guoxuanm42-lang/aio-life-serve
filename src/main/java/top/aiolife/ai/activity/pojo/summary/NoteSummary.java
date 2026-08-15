package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 笔记活动总结统计，承载新增笔记数量和标题。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class NoteSummary {

    private long newCount;

    private List<String> titles = new ArrayList<>();

    /**
     * 判断当前笔记统计是否没有可输出数据。
     *
     * @return 没有新增笔记时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public boolean isEmpty() {
        return newCount == 0;
    }
}
