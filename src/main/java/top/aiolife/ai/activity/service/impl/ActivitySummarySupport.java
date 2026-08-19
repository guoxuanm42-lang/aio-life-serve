package top.aiolife.ai.activity.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.CountItem;
import top.aiolife.ai.activity.support.AiActivitySummaryPolicy;

import java.text.Normalizer;
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
 * @date 2026-08-16
 */
@Slf4j
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

    static <T> List<T> filterValidRecords(
            String moduleName,
            List<T> records,
            Function<T, String> textExtractor
    ) {
        List<T> validRecords = records.stream()
                .filter(record -> isValidCoreText(textExtractor.apply(record)))
                .toList();
        int filteredCount = records.size() - validRecords.size();
        if (filteredCount > 0) {
            log.info("活动报告文本过滤：module={}, filteredCount={}", moduleName, filteredCount);
        }
        return validRecords;
    }

    static boolean isValidCoreText(String text) {
        if (text == null) {
            return false;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .replace("\uFEFF", "")
                .replace("\u200B", "")
                .replace("\u200C", "")
                .replace("\u200D", "");
        if (normalized.isBlank() || normalized.indexOf('\uFFFD') >= 0) {
            return false;
        }
        if (normalized.codePoints().anyMatch(ActivitySummarySupport::isForbiddenControlCharacter)) {
            return false;
        }
        String compact = normalized.codePoints()
                .filter(codePoint -> !Character.isWhitespace(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        long characterCount = compact.codePoints().count();
        if (characterCount >= 3 && compact.codePoints().allMatch(codePoint -> codePoint == '?')) {
            return false;
        }
        if (characterCount >= 3 && isRepeatedPlaceholder(compact)) {
            return false;
        }
        return true;
    }

    private static boolean isForbiddenControlCharacter(int codePoint) {
        return Character.isISOControl(codePoint)
                && codePoint != '\t'
                && codePoint != '\n'
                && codePoint != '\r';
    }

    private static boolean isRepeatedPlaceholder(String compact) {
        int first = compact.codePointAt(0);
        if (first != '*' && first != '_' && first != '-') {
            return false;
        }
        return compact.codePoints().allMatch(codePoint -> codePoint == first);
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
