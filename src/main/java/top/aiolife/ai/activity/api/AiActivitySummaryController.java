package top.aiolife.ai.activity.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryReq;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryGenerateReq;
import top.aiolife.ai.activity.pojo.resp.AiActivitySummaryGenerateResp;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;
import top.aiolife.ai.activity.service.AiActivitySummaryGenerateService;
import top.aiolife.ai.activity.service.AiActivitySummaryService;
import top.aiolife.ai.activity.service.AiActivitySummaryStreamService;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;

/**
 * AI 活动总结控制器，提供结构化统计预览和一次性 AI 总结生成入口。
 *
 * @author Ethan
 * @date 2026-08-16
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/activity-summary")
public class AiActivitySummaryController {

    private static final String SUMMARY_FAILURE_MESSAGE = "活动统计失败，请稍后重试";
    private static final String GENERATE_FAILURE_MESSAGE = "活动总结生成失败，请稍后重试";

    private final AiActivitySummaryService aiActivitySummaryService;
    private final AiActivitySummaryGenerateService aiActivitySummaryGenerateService;
    private final AiActivitySummaryStreamService aiActivitySummaryStreamService;

    /**
     * 预览当前登录用户的统一活动统计。
     *
     * <p>用途：按周、月或年聚合九个业务模块的结构化数据，不调用 AI 模型且不保存消息。</p>
     *
     * @param req 活动总结请求，period 仅支持 week、month、year，conversationId 本阶段不使用
     * @return 统一返回结构，data 为已过滤空模块的活动统计上下文
     *
     * @author Ethan
     * @date 2026-08-13
     */
    @PostMapping("/preview")
    public ApiResponse<AiActivitySummaryContext> preview(@RequestBody AiActivitySummaryReq req) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiActivitySummaryService.summarize(userId, req));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, exception.getMessage());
        } catch (Exception exception) {
            log.error("Failed to preview AI activity summary", exception);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, SUMMARY_FAILURE_MESSAGE);
        }
    }

    /**
     * 为当前登录用户的指定会话生成并保存活动总结。
     *
     * <p>用途：按周、月或年统计活动，调用会话 Agent 一次，并原子保存用户指令与助手回复。</p>
     *
     * @param req 生成请求，包含 period、conversationId 和 UUID 幂等键
     * @return 统一返回结构，data 为消息 ID、原始用户指令、助手总结和实际模型信息
     *
     * @author Ethan
     * @date 2026-08-16
     */
    @PostMapping("/generate")
    public ApiResponse<AiActivitySummaryGenerateResp> generate(@RequestBody AiActivitySummaryGenerateReq req) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiActivitySummaryGenerateService.generate(userId, req));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, exception.getMessage());
        } catch (Exception exception) {
            log.error("Failed to generate AI activity summary", exception);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, GENERATE_FAILURE_MESSAGE);
        }
    }

    /**
     * 为当前登录用户异步生成活动总结并流式返回真实阶段进度。
     *
     * <p>用途：前端提交周期、会话 ID 和 UUID 幂等键，接口依次返回 progress、done 或 error SSE 事件；
     * 客户端断开不会取消已经开始的后台生成任务。</p>
     *
     * @param req 生成请求，包含 period、conversationId 和 UUID 幂等键
     * @return SSE 发送器；progress 为阶段信息，done 为完整生成响应，error 为结构化错误
     *
     * @author Ethan
     * @date 2026-08-16
     */
    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStream(@RequestBody AiActivitySummaryGenerateReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        return aiActivitySummaryStreamService.generateStream(userId, req);
    }
}
