package top.aiolife.ai.activity.api;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryReq;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;
import top.aiolife.ai.activity.service.AiActivitySummaryService;
import top.aiolife.ai.activity.service.AiActivitySummaryGenerateService;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 活动总结预览控制器单元测试，验证登录用户传递和异常响应转换。
 *
 * @author Ethan
 * @date 2026-08-13
 */
class AiActivitySummaryControllerTest {

    private static final Long USER_ID = 101L;

    @Test
    void shouldPreviewSummaryForCurrentLoginUser() {
        AiActivitySummaryService service = mock(AiActivitySummaryService.class);
        AiActivitySummaryController controller = new AiActivitySummaryController(
                service, mock(AiActivitySummaryGenerateService.class));
        AiActivitySummaryReq req = request("week");
        AiActivitySummaryContext context = new AiActivitySummaryContext();
        context.setPeriod("week");
        when(service.summarize(USER_ID, req)).thenReturn(context);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            ApiResponse<AiActivitySummaryContext> response = controller.preview(req);

            assertEquals(ResponseCodeConst.RSCODE_SUCCESS, response.getRscode());
            assertSame(context, response.getData());
            verify(service).summarize(USER_ID, req);
        }
    }

    @Test
    void shouldReturnParameterErrorForInvalidRequest() {
        AiActivitySummaryService service = mock(AiActivitySummaryService.class);
        AiActivitySummaryController controller = new AiActivitySummaryController(
                service, mock(AiActivitySummaryGenerateService.class));
        AiActivitySummaryReq req = request("today");
        when(service.summarize(USER_ID, req)).thenThrow(new IllegalArgumentException("非法周期"));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            ApiResponse<AiActivitySummaryContext> response = controller.preview(req);

            assertEquals(ResponseCodeConst.RECODE_PARAM_FAIL, response.getRscode());
            assertEquals("非法周期", response.getResult());
            assertNull(response.getData());
        }
    }

    @Test
    void shouldReturnGenericMessageForInternalFailure() {
        AiActivitySummaryService service = mock(AiActivitySummaryService.class);
        AiActivitySummaryController controller = new AiActivitySummaryController(
                service, mock(AiActivitySummaryGenerateService.class));
        AiActivitySummaryReq req = request("week");
        when(service.summarize(USER_ID, req)).thenThrow(new IllegalStateException("数据库明细"));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            ApiResponse<AiActivitySummaryContext> response = controller.preview(req);

            assertEquals(ResponseCodeConst.RSCODE_COMMON_FAIL, response.getRscode());
            assertEquals("活动统计失败，请稍后重试", response.getResult());
            assertNull(response.getData());
        }
    }

    private static AiActivitySummaryReq request(String period) {
        AiActivitySummaryReq req = new AiActivitySummaryReq();
        req.setPeriod(period);
        return req;
    }
}
