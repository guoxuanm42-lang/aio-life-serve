package top.aiolife.ai.activity.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.aiolife.ai.activity.model.AiActivitySummaryProgressStage;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryGenerateReq;
import top.aiolife.ai.activity.pojo.resp.AiActivitySummaryGenerateResp;
import top.aiolife.ai.activity.service.AiActivitySummaryGenerateService;
import top.aiolife.ai.activity.service.AiActivitySummaryStreamService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 活动总结流式进度服务实现，在独立线程池中执行原有同步生成链路并发送 SSE 事件。
 *
 * @author Ethan
 * @date 2026-08-16
 */
@Slf4j
@Service
public class AiActivitySummaryStreamServiceImpl implements AiActivitySummaryStreamService {

    private static final long SSE_TIMEOUT_MILLIS = 300_000L;
    private static final String GENERATE_FAILURE_MESSAGE = "活动总结生成失败，请稍后重试";
    private final AiActivitySummaryGenerateService generateService;
    private final Executor taskExecutor;

    AiActivitySummaryStreamServiceImpl(
            AiActivitySummaryGenerateService generateService,
            @Qualifier("activitySummaryTaskExecutor") Executor taskExecutor) {
        this.generateService = generateService;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 异步生成活动总结，并按真实业务边界发送 progress、done 或 error 事件。
     *
     * <p>客户端断开只停止发送事件，不中断已经开始的生成任务。</p>
     *
     * @param userId 当前用户 ID
     * @param req 活动总结生成请求
     * @return 活动总结生成事件流
     *
     * @author Ethan
     * @date 2026-08-16
     */
    @Override
    public SseEmitter generateStream(Long userId, AiActivitySummaryGenerateReq req) {
        SseEmitter emitter = createEmitter();
        AtomicBoolean connected = new AtomicBoolean(true);
        emitter.onTimeout(() -> disconnect(emitter, connected, "Activity summary SSE timed out"));
        emitter.onError(error -> disconnect(emitter, connected, "Activity summary SSE client disconnected"));
        emitter.onCompletion(() -> connected.set(false));

        try {
            taskExecutor.execute(() -> runGeneration(userId, req, emitter, connected));
        } catch (TaskRejectedException exception) {
            sendError(emitter, connected, "ACTIVITY_SUMMARY_BUSY", "生成任务较多，请稍后重试", true, exception);
        }
        return emitter;
    }

    protected SseEmitter createEmitter() {
        return new SseEmitter(SSE_TIMEOUT_MILLIS);
    }

    private void runGeneration(
            Long userId,
            AiActivitySummaryGenerateReq req,
            SseEmitter emitter,
            AtomicBoolean connected) {
        try {
            AiActivitySummaryGenerateResp response = generateService.generate(
                    userId, req, stage -> sendProgress(emitter, connected, stage));
            if (sendEvent(emitter, connected, "done", response)) {
                emitter.complete();
            }
        } catch (IllegalArgumentException exception) {
            sendError(emitter, connected, "INVALID_REQUEST", exception.getMessage(), false, exception);
        } catch (Exception exception) {
            sendError(emitter, connected, "ACTIVITY_SUMMARY_FAILED", GENERATE_FAILURE_MESSAGE, true, exception);
        }
    }

    private void sendProgress(
            SseEmitter emitter,
            AtomicBoolean connected,
            AiActivitySummaryProgressStage stage) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("stage", stage.name());
        payload.put("label", stage.getLabel());
        payload.put("percent", stage.getPercent());
        sendEvent(emitter, connected, "progress", payload);
    }

    private boolean sendEvent(SseEmitter emitter, AtomicBoolean connected, String eventName, Object data) {
        if (!connected.get()) {
            return false;
        }
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
            return true;
        } catch (IOException | IllegalStateException exception) {
            connected.set(false);
            log.info("Activity summary SSE client disconnected while sending {} event", eventName);
            return false;
        }
    }

    private void sendError(
            SseEmitter emitter,
            AtomicBoolean connected,
            String code,
            String message,
            boolean retryable,
            Throwable exception) {
        log.error("Activity summary generation failed: {}", exception.getMessage(), exception);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", code);
        payload.put("message", message);
        payload.put("retryable", retryable);
        if (sendEvent(emitter, connected, "error", payload)) {
            emitter.complete();
        }
    }

    private void disconnect(SseEmitter emitter, AtomicBoolean connected, String reason) {
        if (connected.compareAndSet(true, false)) {
            log.info(reason);
            emitter.complete();
        }
    }
}
