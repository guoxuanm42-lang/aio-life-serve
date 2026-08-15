package top.aiolife.ai.activity.model;

import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * AI 活动总结统计周期。
 *
 * @author Ethan
 * @date 2026-08-12
 */
public enum AiActivitySummaryPeriod {

    WEEK,
    MONTH,
    YEAR;

    /**
     * 将请求值转换为统计周期，严格要求使用小写协议值。
     *
     * @param value 请求中的周期值
     * @return 对应的统计周期
     * @throws IllegalArgumentException 周期为空或不是 week、month、year 时抛出
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public static AiActivitySummaryPeriod fromValue(String value) {
        if (!StringUtils.hasText(value) || !value.equals(value.trim()) || !value.equals(value.toLowerCase(Locale.ROOT))) {
            throw invalidPeriod();
        }
        return switch (value) {
            case "week" -> WEEK;
            case "month" -> MONTH;
            case "year" -> YEAR;
            default -> throw invalidPeriod();
        };
    }

    /**
     * 返回前后端协议使用的小写周期值。
     *
     * @return week、month 或 year
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    private static IllegalArgumentException invalidPeriod() {
        return new IllegalArgumentException("总结周期必须为 week、month 或 year");
    }
}
