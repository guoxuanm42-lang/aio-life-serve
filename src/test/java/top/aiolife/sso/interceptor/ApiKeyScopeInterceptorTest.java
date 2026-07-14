package top.aiolife.sso.interceptor;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.aiolife.sso.constant.ApiKeyAuthConstants;
import top.aiolife.sso.exception.ApiKeyAccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * API Key 访问范围拦截器测试，验证 MCP 精确端点、允许方法及非 API Key 请求放行规则。
 *
 * @author Ethan
 * @date 2026-07-14
 */
class ApiKeyScopeInterceptorTest {

    private static final String ACCESS_DENIED_MESSAGE = "API Key 仅允许访问 MCP 协议端点";

    @Test
    void shouldAllowSupportedMethodsOnExactMcpPath() throws Exception {
        for (String method : new String[]{"GET", "POST", "DELETE"}) {
            assertTrue(invokeWithApiKey(method, "/mcp"), method);
        }
    }

    @Test
    void shouldRejectOrdinaryAndMcpManagementPaths() {
        String[] forbiddenPaths = {
                "/article/query",
                "/user/current",
                "/api-key/list",
                "/mcp/tools",
                "/mcp/tools/thought_query/call",
                "/mcp/tools/thought_query/config",
                "/mcp/tools/thought_query/status",
                "/mcp/tools/thought_query/logs",
                "/mcp/",
                "/mcp/anything"
        };

        for (String path : forbiddenPaths) {
            ApiKeyAccessDeniedException exception = assertThrows(
                    ApiKeyAccessDeniedException.class,
                    () -> invokeWithApiKey("POST", path),
                    path
            );
            assertEquals(ACCESS_DENIED_MESSAGE, exception.getMessage());
        }
    }

    @Test
    void shouldRejectUnsupportedMethodsOnMcpPath() {
        for (String method : new String[]{"PUT", "PATCH"}) {
            ApiKeyAccessDeniedException exception = assertThrows(
                    ApiKeyAccessDeniedException.class,
                    () -> invokeWithApiKey(method, "/mcp"),
                    method
            );
            assertEquals(ACCESS_DENIED_MESSAGE, exception.getMessage());
        }
    }

    @Test
    void shouldSkipScopeCheckForTokenAndAnonymousRequests() throws Exception {
        assertTrue(invokeWithoutApiKey("POST", "/article/query", false));
        assertTrue(invokeWithoutApiKey("POST", "/article/query", null));
    }

    private boolean invokeWithApiKey(String method, String servletPath) throws Exception {
        return invoke(method, servletPath, true);
    }

    private boolean invokeWithoutApiKey(String method, String servletPath, Boolean marker) throws Exception {
        return invoke(method, servletPath, marker);
    }

    private boolean invoke(String method, String servletPath, Boolean marker) throws Exception {
        SaStorage storage = mock(SaStorage.class);
        when(storage.get(ApiKeyAuthConstants.IS_API_KEY_AUTH_STORAGE_KEY)).thenReturn(marker);
        MockHttpServletRequest request = new MockHttpServletRequest(method, servletPath);
        request.setServletPath(servletPath);

        try (MockedStatic<SaHolder> saHolder = mockStatic(SaHolder.class)) {
            saHolder.when(SaHolder::getStorage).thenReturn(storage);
            return new ApiKeyScopeInterceptor().preHandle(
                    request,
                    new MockHttpServletResponse(),
                    new Object()
            );
        }
    }
}
