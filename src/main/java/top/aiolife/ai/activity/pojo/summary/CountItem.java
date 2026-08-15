package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

/**
 * AI 活动统计通用计数项，用于表达类型、主题、分类和难度分布。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class CountItem {

    private String key;

    private String name;

    private long count;
}
