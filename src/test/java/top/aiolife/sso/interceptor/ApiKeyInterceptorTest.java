package top.aiolife.sso.interceptor;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.aiolife.sso.pojo.entity.ApiKeyEntity;
import top.aiolife.sso.service.IApiKeyLogService;
import top.aiolife.sso.service.IApiKeyService;
import top.aiolife.sso.util.ApiKeyMaskUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * API Key 认证拦截器测试，验证认证失败日志脱敏及有效凭证身份切换行为。
 *
 * @author Ethan
 * @date 2026-07-14
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiKeyInterceptorTest {

    private static final String API_KEY = "ak-1234567890abcdef";
    private static final String MASKED_API_KEY = "ak-12345***cdef";

    @Mock
    private IApiKeyService apiKeyService;

    @Mock
    private IApiKeyLogService apiKeyLogService;

    private Logger logger;

    private MemoryAppender appender;

    @BeforeEach
    void attachMemoryAppender() throws ClassNotFoundException {
        Class.forName(ApiKeyInterceptor.class.getName());
        logger = (Logger) LogManager.getLogger(ApiKeyInterceptor.class);
        logger.setLevel(Level.WARN);
        appender = new MemoryAppender();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachMemoryAppender() {
        logger.removeAppender(appender);
        appender.stop();
    }

    @Test
    @Order(2)
    void shouldMaskMissingApiKeyInLogAndException() {
        when(apiKeyService.getByApiKey(API_KEY)).thenReturn(null);

        NotLoginException exception = assertThrows(
                NotLoginException.class,
                () -> interceptor().preHandle(request(API_KEY), new MockHttpServletResponse(), new Object())
        );

        assertMaskedFailure("不存在", exception);
    }

    @Test
    @Order(3)
    void shouldMaskDeletedApiKeyInLogAndException() {
        ApiKeyEntity entity = apiKeyEntity();
        entity.setIsDeleted(1);
        when(apiKeyService.getByApiKey(API_KEY)).thenReturn(entity);

        NotLoginException exception = assertThrows(
                NotLoginException.class,
                () -> interceptor().preHandle(request(API_KEY), new MockHttpServletResponse(), new Object())
        );

        assertMaskedFailure("不存在", exception);
    }

    @Test
    @Order(4)
    void shouldMaskExpiredApiKeyInLogAndException() {
        ApiKeyEntity entity = apiKeyEntity();
        entity.setExpiredAt(LocalDateTime.now().minusMinutes(1));
        when(apiKeyService.getByApiKey(API_KEY)).thenReturn(entity);

        NotLoginException exception = assertThrows(
                NotLoginException.class,
                () -> interceptor().preHandle(request(API_KEY), new MockHttpServletResponse(), new Object())
        );

        assertMaskedFailure("已过期", exception);
    }

    @Test
    @Order(1)
    void shouldKeepValidApiKeyAuthenticationBehavior() throws Exception {
        ApiKeyEntity entity = apiKeyEntity();
        when(apiKeyService.getByApiKey(API_KEY)).thenReturn(entity);
        SaStorage storage = mock(SaStorage.class);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
             MockedStatic<SaHolder> saHolder = mockStatic(SaHolder.class)) {
            saHolder.when(SaHolder::getStorage).thenReturn(storage);

            boolean allowed = interceptor().preHandle(request(API_KEY), new MockHttpServletResponse(), new Object());

            assertTrue(allowed);
            stpUtil.verify(() -> StpUtil.switchTo(entity.getUserId()));
            verify(storage).set("API_KEY_ID", entity.getId());
            verify(storage).set("IS_API_KEY_AUTH", true);
        }
    }

    private void assertMaskedFailure(String reason, NotLoginException exception) {
        String logs = String.join(System.lineSeparator(), appender.messages());
        assertTrue(logs.contains("API Key " + MASKED_API_KEY + " " + reason), logs);
        assertTrue(!logs.contains(API_KEY), logs);
        assertEquals("API_KEY", exception.getLoginType());
        assertEquals(ApiKeyMaskUtil.mask(API_KEY), exception.getType());
    }

    private ApiKeyInterceptor interceptor() {
        return new ApiKeyInterceptor(apiKeyService, apiKeyLogService);
    }

    private MockHttpServletRequest request(String apiKey) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + apiKey);
        return request;
    }

    private ApiKeyEntity apiKeyEntity() {
        ApiKeyEntity entity = new ApiKeyEntity();
        entity.setId(101L);
        entity.setUserId(202L);
        entity.setApiKey(API_KEY);
        entity.setIsDeleted(0);
        return entity;
    }

    private static final class MemoryAppender extends AbstractAppender {

        private final List<String> messages = new CopyOnWriteArrayList<>();

        private MemoryAppender() {
            super("api-key-interceptor-test-" + System.nanoTime(), null, null, false, null);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        private List<String> messages() {
            return messages;
        }
    }
}
