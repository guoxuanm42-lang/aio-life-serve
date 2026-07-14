package top.aiolife.sso.exception;

/**
 * API Key 访问范围异常，表示有效凭证尝试访问 MCP 协议端点之外的资源。
 *
 * @author Ethan
 * @date 2026-07-14
 */
public class ApiKeyAccessDeniedException extends RuntimeException {

    private static final String ACCESS_DENIED_MESSAGE = "API Key 仅允许访问 MCP 协议端点";

    /**
     * 创建 API Key 访问范围异常。
     *
     * @author Ethan
     * @date 2026-07-14
     */
    public ApiKeyAccessDeniedException() {
        super(ACCESS_DENIED_MESSAGE);
    }
}
