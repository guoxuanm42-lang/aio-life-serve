package top.aiolife.sso.util;

/**
 * API Key 脱敏工具，统一认证日志和接口展示中的凭证隐藏规则。
 *
 * @author Ethan
 * @date 2026-07-14
 */
public final class ApiKeyMaskUtil {

    private static final int PREFIX_LENGTH = 8;
    private static final int SUFFIX_LENGTH = 4;
    private static final String MASK = "***";

    private ApiKeyMaskUtil() {
    }

    /**
     * 对 API Key 进行脱敏，正常值仅保留前八位和后四位，异常短值完全隐藏。
     *
     * @param apiKey 待脱敏的 API Key
     * @return 脱敏后的 API Key；空值或长度不足十二位时返回三个星号
     *
     * @author Ethan
     * @date 2026-07-14
     */
    public static String mask(String apiKey) {
        if (apiKey == null || apiKey.isBlank() || apiKey.length() < PREFIX_LENGTH + SUFFIX_LENGTH) {
            return MASK;
        }
        return apiKey.substring(0, PREFIX_LENGTH) + MASK + apiKey.substring(apiKey.length() - SUFFIX_LENGTH);
    }
}
