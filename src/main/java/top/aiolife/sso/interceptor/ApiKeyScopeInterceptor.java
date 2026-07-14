package top.aiolife.sso.interceptor;

import cn.dev33.satoken.context.SaHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.aiolife.sso.constant.ApiKeyAuthConstants;
import top.aiolife.sso.exception.ApiKeyAccessDeniedException;

import java.util.Locale;
import java.util.Set;

/**
 * API Key 访问范围拦截器，限制 API Key 仅能访问 MCP 协议端点。
 *
 * @author Ethan
 * @date 2026-07-14
 */
@Component
public class ApiKeyScopeInterceptor implements HandlerInterceptor {

    private static final String MCP_PROTOCOL_PATH = "/mcp";

    private static final Set<String> MCP_PROTOCOL_METHODS = Set.of("GET", "POST", "DELETE");

    /**
     * 校验已通过 API Key 认证的请求是否精确指向 MCP 协议端点。
     *
     * @param request Http 请求对象，用于获取应用内部路径
     * @param response Http 响应对象
     * @param handler 当前请求处理器
     * @return true，表示非 API Key 请求或当前路径为 MCP 协议端点
     * @throws ApiKeyAccessDeniedException API Key 访问 MCP 协议端点之外的路径时抛出
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        boolean apiKeyAuthenticated = Boolean.TRUE.equals(
                SaHolder.getStorage().get(ApiKeyAuthConstants.IS_API_KEY_AUTH_STORAGE_KEY)
        );
        if (!apiKeyAuthenticated) {
            return true;
        }
        if (MCP_PROTOCOL_PATH.equals(request.getServletPath())
                && MCP_PROTOCOL_METHODS.contains(request.getMethod().toUpperCase(Locale.ROOT))) {
            return true;
        }
        throw new ApiKeyAccessDeniedException();
    }
}
