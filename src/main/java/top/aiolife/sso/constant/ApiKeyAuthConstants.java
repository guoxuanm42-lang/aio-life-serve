package top.aiolife.sso.constant;

/**
 * API Key 认证上下文常量，统一定义请求内身份标识和凭证信息存储键。
 *
 * @author Ethan
 * @date 2026-07-14
 */
public final class ApiKeyAuthConstants {

    public static final String API_KEY_ID_STORAGE_KEY = "API_KEY_ID";

    public static final String IS_API_KEY_AUTH_STORAGE_KEY = "IS_API_KEY_AUTH";

    public static final String AUTH_TYPE_STORAGE_KEY = "AUTH_TYPE";

    public static final String API_KEY_AUTH_TYPE = "API_KEY";

    private ApiKeyAuthConstants() {
    }
}
