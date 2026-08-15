package top.aiolife.ai.activity.service.impl;

import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.CountItem;
import top.aiolife.ai.activity.support.AiActivitySummaryPolicy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 基础内容活动统计的内部公共支持类，统一参数校验、明细截断和分布项构造规则。
 *
 * @author Ethan
 * @date 2026-08-13
 */
final class ActivitySummarySupport {

    static final String UNCATEGORIZED_KEY = "uncategorized";
    static final String UNCATEGORIZED_NAME = "未分类";
    static final String UNSET_KEY = "unset";
    static final String UNSET_NAME = "未设置";

    private ActivitySummarySupport() {
    }

    static void validate(Long userId, AiActivityDateRange range) {
        if (userId == null) {
            throw new IllegalArgumentException("用户 ID 不能为空");
        }
        if (range == null || range.getStartTime() == null || range.getEndTime() == null) {
            throw new IllegalArgumentException("活动统计时间范围不能为空");
        }
        if (!range.getStartTime().isBefore(range.getEndTime())) {
            throw new IllegalArgumentException("活动统计开始时间必须早于结束时间");
        }
    }

    static <T> List<String> detailTexts(List<T> records, Function<T, String> textExtractor) {
        return records.stream()
                .map(textExtractor)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .limit(AiActivitySummaryPolicy.TITLE_DETAIL_LIMIT)
                .toList();
    }

    static List<CountItem> buildDistribution(
            Map<String, Long> counts,
            Function<String, String> nameResolver
    ) {
        List<CountItem> items = new ArrayList<>();
        counts.forEach((key, count) -> {
            CountItem item = new CountItem();
            item.setKey(key);
            item.setName(nameResolver.apply(key));
            item.setCount(count);
            items.add(item);
        });
        items.sort(Comparator.comparingLong(CountItem::getCount).reversed()
                .thenComparing(CountItem::getName)
                .thenComparing(CountItem::getKey));
        return items;
    }

    static Map<String, Long> countKeys(List<String> keys) {
        Map<String, Long> counts = new LinkedHashMap<>();
        keys.forEach(key -> counts.merge(key, 1L, Long::sum));
        return counts;
    }
}
