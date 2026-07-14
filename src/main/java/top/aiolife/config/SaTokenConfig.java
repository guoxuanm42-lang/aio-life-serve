package top.aiolife.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.context.second.SaTokenSecondContext;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.aiolife.sso.constant.ApiKeyAuthConstants;
import top.aiolife.sso.interceptor.ApiKeyInterceptor;
import top.aiolife.sso.interceptor.ApiKeyScopeInterceptor;
import top.aiolife.sso.interceptor.UserLastActiveInterceptor;

/**
 * Sa-Token Web 认证配置，负责线程上下文适配以及 Token、API Key 和用户活跃时间拦截器编排。
 *
 * @author Ethan
 * @date 2026-07-14
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor;
    private final ApiKeyScopeInterceptor apiKeyScopeInterceptor;
    private final UserLastActiveInterceptor userLastActiveInterceptor;

    /**
     * 初始化 Sa-Token 第二上下文，使 MCP 等线程切换场景可读取当前请求和身份存储。
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @PostConstruct
    public void initSecondContext() {
        SaTokenContextForThreadLocal threadLocalContext = new SaTokenContextForThreadLocal();
        SaManager.setSaTokenSecondContext(new SaTokenSecondContext() {
            @Override
            public SaRequest getRequest() {
                return threadLocalContext.getRequest();
            }

            @Override
            public SaResponse getResponse() {
                return threadLocalContext.getResponse();
            }

            @Override
            public SaStorage getStorage() {
                return threadLocalContext.getStorage();
            }

            @Override
            public boolean matchPath(String pattern, String path) {
                return threadLocalContext.matchPath(pattern, path);
            }

            @Override
            public boolean isValid() {
                return threadLocalContext.isValid();
            }
        });
    }

    /**
     * 注册并排序 Web 认证拦截器，先识别 API Key，再校验访问范围和登录状态。
     *
     * @param registry Spring MVC 拦截器注册器
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // API Key 拦截器，需在 Sa-Token 拦截器之前执行
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/register", "/auth/sendEmailCode", "/auth/sendResetPasswordCode",
                        "/auth/resetPassword",
                         "/actuator/**");

        // API Key 访问范围拦截器，仅允许访问 MCP 协议端点
        registry.addInterceptor(apiKeyScopeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/register", "/auth/sendEmailCode", "/auth/sendResetPasswordCode",
                        "/auth/resetPassword",
                        "/actuator/**");

        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
            // CORS 预检请求直接放行
            if (RequestMethod.OPTIONS.name().equalsIgnoreCase(SaHolder.getRequest().getMethod())) {
                return;
            }
            // 如果已经通过 API Key 认证了，就不要再 checkLogin 了（实现 API Key 或 Token 二选一）
            if (Boolean.TRUE.equals(SaHolder.getStorage().get(ApiKeyAuthConstants.IS_API_KEY_AUTH_STORAGE_KEY))) {
                return;
            }
            StpUtil.checkLogin();
        })).addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/register", "/auth/sendEmailCode", "/auth/sendResetPasswordCode",
                        "/auth/resetPassword",
                        "/actuator/**",
                        "/file/preview/**");

        // 记录最后活跃时间（仅 Token 请求），需在 Sa-Token 校验通过后执行
        registry.addInterceptor(userLastActiveInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/register", "/auth/sendEmailCode", "/auth/sendResetPasswordCode",
                        "/auth/resetPassword",
                        "/actuator/**",
                        "/file/preview/**");
    }
}
