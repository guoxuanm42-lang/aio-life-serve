package top.aiolife.ai.activity.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryGenerateReq;

/**
 * 活动总结流式进度服务，负责通过 SSE 输出真实生成阶段和最终结果。
 *
 * @author Ethan
 * @date 2026-08-16
 */
public interface AiActivitySummaryStreamService {

    /**
     * 异步生成活动总结并返回阶段进度事件流。
     *
     * @param userId 当前用户 ID
     * @param req 活动总结生成请求
     * @return 输出 progress、done 和 error 事件的 SSE 发送器
     *
     * @author Ethan
     * @date 2026-08-16
     */
    SseEmitter generateStream(Long userId, AiActivitySummaryGenerateReq req);
}
