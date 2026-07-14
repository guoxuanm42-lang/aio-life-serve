package top.aiolife.sso.interceptor;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.aiolife.sso.constant.ApiKeyAuthConstants;
import top.aiolife.sso.pojo.entity.ApiKeyEntity;
import top.aiolife.sso.service.IApiKeyLogService;
import top.aiolife.sso.service.IApiKeyService;
import top.aiolife.sso.util.ApiKeyMaskUtil;

import java.time.LocalDateTime;

/**
 * API Key 认证拦截器，负责凭证校验、请求内身份切换与调用日志记录。
 *
 * @author Ethan
 * @date 2026-07-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final IApiKeyService apiKeyService;
    private final IApiKeyLogService apiKeyLogService;

    /**
     * 在请求处理前校验 API Key，并将当前请求临时切换为凭证所属用户。
     *
     * @param request Http 请求对象，用于读取 Authorization 请求头
     * @param response Http 响应对象
     * @param handler 当前请求处理器
     * @return true，表示通过校验或当前请求未使用 API Key
     * @throws Exception API Key 无效或已过期时抛出未登录异常
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 1. 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");
        if (StrUtil.isBlank(authHeader)) {
            return true;
        }

        // 2. 检查是否为 API Key 格式 (ak- 开头)
        String apiKeyStr = authHeader.replace("Bearer ", "").trim();
        if (!apiKeyStr.startsWith("ak-")) {
            return true;
        }

        // 3. 校验 API Key
        ApiKeyEntity apiKeyEntity = apiKeyService.getByApiKey(apiKeyStr);
        if (apiKeyEntity == null || apiKeyEntity.getIsDeleted() == 1) {
            String maskedApiKey = ApiKeyMaskUtil.mask(apiKeyStr);
            log.warn("API Key {} 不存在", maskedApiKey);
            throw new NotLoginException("API Key 无效", "API_KEY", maskedApiKey);
        }

        // 4. 检查是否过期
        if (apiKeyEntity.getExpiredAt() != null && apiKeyEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
            String maskedApiKey = ApiKeyMaskUtil.mask(apiKeyStr);
            log.warn("API Key {} 已过期", maskedApiKey);
            throw new NotLoginException("API Key 已过期", "API_KEY", maskedApiKey);
        }

        // 5. 临时身份切换 (仅限本次请求上下文，不产生真实会话)
        StpUtil.switchTo(apiKeyEntity.getUserId());
        
        // 6. 将认证信息存入 SaStorage，以便后续 SaInterceptor 跳过校验
        SaHolder.getStorage().set(ApiKeyAuthConstants.API_KEY_ID_STORAGE_KEY, apiKeyEntity.getId());
        SaHolder.getStorage().set(ApiKeyAuthConstants.IS_API_KEY_AUTH_STORAGE_KEY, true);
        SaHolder.getStorage().set(ApiKeyAuthConstants.AUTH_TYPE_STORAGE_KEY, ApiKeyAuthConstants.API_KEY_AUTH_TYPE);
        
        return true;
    }

    /**
     * 在请求完成后记录 API Key 调用日志，并清理临时用户身份。
     *
     * @param request Http 请求对象，用于读取路径、方法和客户端 IP
     * @param response Http 响应对象，用于读取响应状态码
     * @param handler 当前请求处理器
     * @param ex 请求处理期间抛出的异常，无异常时为 null
     * @throws Exception 调用日志记录或身份清理失败时抛出
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        Boolean isApiKeyAuth = (Boolean) SaHolder.getStorage().get(ApiKeyAuthConstants.IS_API_KEY_AUTH_STORAGE_KEY);
        if (Boolean.TRUE.equals(isApiKeyAuth)) {
            Long apiKeyId = (Long) SaHolder.getStorage().get(ApiKeyAuthConstants.API_KEY_ID_STORAGE_KEY);
            // 记录调用日志
            apiKeyLogService.log(
                    apiKeyId,
                    request.getRequestURI(),
                    request.getMethod(),
                    response.getStatus(),
                    getIpAddress(request)
            );
            // 本次请求结束，结束身份切换，保持会话清洁
            StpUtil.endSwitch();
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
