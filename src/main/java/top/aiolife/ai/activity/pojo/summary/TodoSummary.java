package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 待办活动总结统计，承载新增待办数量和内容。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class TodoSummary {

    private long newCount;

    private List<String> contents = new ArrayList<>();

    /**
     * 判断当前待办统计是否没有可输出数据。
     *
     * @return 没有新增待办时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public boolean isEmpty() {
        return newCount == 0;
    }
}
