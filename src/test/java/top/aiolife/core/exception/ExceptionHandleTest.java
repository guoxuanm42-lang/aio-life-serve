package top.aiolife.core.exception;

import cn.dev33.satoken.exception.NotLoginException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import top.aiolife.sso.exception.ApiKeyAccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 全局异常处理器测试，验证 API Key 越权响应状态与凭证安全边界。
 *
 * @author Ethan
 * @date 2026-07-14
 */
class ExceptionHandleTest {

    @Test
    void shouldKeepInvalidApiKeyResponseUnauthorized() {
        NotLoginException exception = new NotLoginException("API Key 无效", "API_KEY", "***");

        ResponseEntity<String> response = new ExceptionHandle().handleUnauthorizedException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("未授权: API Key 无效", response.getBody());
    }

    @Test
    void shouldReturnForbiddenWithoutApiKeyValue() {
        String fullApiKey = "ak-1234567890abcdef";
        ApiKeyAccessDeniedException exception = new ApiKeyAccessDeniedException();

        ResponseEntity<String> response = new ExceptionHandle()
                .handleApiKeyAccessDeniedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("API Key 仅允许访问 MCP 协议端点", response.getBody());
        assertFalse(response.getBody().contains(fullApiKey));
    }
}
