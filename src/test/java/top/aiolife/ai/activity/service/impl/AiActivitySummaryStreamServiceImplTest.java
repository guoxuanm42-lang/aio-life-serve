package top.aiolife.ai.activity.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.aiolife.ai.activity.model.AiActivitySummaryProgressStage;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryGenerateReq;
import top.aiolife.ai.activity.pojo.resp.AiActivitySummaryGenerateResp;
import top.aiolife.ai.activity.service.AiActivitySummaryGenerateService;
import top.aiolife.ai.activity.service.AiActivitySummaryProgressListener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 活动总结 SSE 服务测试，验证真实阶段输出以及客户端断开后的后台生成行为。
 *
 * @author Ethan
 * @date 2026-08-16
 */
class AiActivitySummaryStreamServiceImplTest {

    private static final Long USER_ID = 10L;

    @Test
    void shouldSendProgressAndDoneEventsFromSingleGenerationCall() throws IOException {
        AiActivitySummaryGenerateService generateService = mock(AiActivitySummaryGenerateService.class);
        SseEmitter emitter = mock(SseEmitter.class);
        doNothing().when(emitter).send(any(SseEmitter.SseEventBuilder.class));
        AiActivitySummaryGenerateResp response = AiActivitySummaryGenerateResp.builder()
                .conversationId(20L).period("week").assistantMessageId(2L).build();
        when(generateService.generate(eq(USER_ID), any(), any(AiActivitySummaryProgressListener.class)))
                .thenAnswer(invocation -> {
                    AiActivitySummaryProgressListener listener = invocation.getArgument(2);
                    listener.onProgress(AiActivitySummaryProgressStage.COLLECTING);
                    listener.onProgress(AiActivitySummaryProgressStage.GENERATING);
                    listener.onProgress(AiActivitySummaryProgressStage.COMPLETED);
                    return response;
                });
        AiActivitySummaryStreamServiceImpl service = service(generateService, Runnable::run, emitter);

        service.generateStream(USER_ID, request());

        ArgumentCaptor<SseEmitter.SseEventBuilder> events =
                ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        verify(emitter, org.mockito.Mockito.times(4)).send(events.capture());
        List<Object> data = events.getAllValues().stream()
                .flatMap(event -> event.build().stream())
                .map(item -> item.getData())
                .toList();
        assertTrue(data.stream().anyMatch(item -> item.toString().contains("event:progress")), data.toString());
        assertTrue(data.stream().anyMatch(item -> item.toString().contains("event:done")), data.toString());
        assertTrue(data.stream().anyMatch(item -> item instanceof java.util.Map<?, ?> map
                && Integer.valueOf(55).equals(map.get("percent"))));
        assertTrue(data.contains(response));
        verify(generateService).generate(eq(USER_ID), any(), any(AiActivitySummaryProgressListener.class));
        verify(emitter).complete();
    }

    @Test
    void shouldContinueGenerationWithoutSendingAfterClientDisconnects() {
        AiActivitySummaryGenerateService generateService = mock(AiActivitySummaryGenerateService.class);
        SseEmitter emitter = mock(SseEmitter.class);
        AtomicReference<Runnable> task = new AtomicReference<>();
        AtomicReference<Consumer<Throwable>> errorHandler = new AtomicReference<>();
        org.mockito.Mockito.doAnswer(invocation -> {
            errorHandler.set(invocation.getArgument(0));
            return null;
        }).when(emitter).onError(any());
        Executor executor = task::set;
        when(generateService.generate(eq(USER_ID), any(), any(AiActivitySummaryProgressListener.class)))
                .thenReturn(AiActivitySummaryGenerateResp.builder().period("week").build());
        AiActivitySummaryStreamServiceImpl service = service(generateService, executor, emitter);

        service.generateStream(USER_ID, request());
        errorHandler.get().accept(new IOException("client closed"));
        task.get().run();

        verify(generateService).generate(eq(USER_ID), any(), any(AiActivitySummaryProgressListener.class));
        try {
            verify(emitter, never()).send(any(SseEmitter.SseEventBuilder.class));
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private AiActivitySummaryStreamServiceImpl service(
            AiActivitySummaryGenerateService generateService,
            Executor executor,
            SseEmitter emitter) {
        return new AiActivitySummaryStreamServiceImpl(generateService, executor) {
            @Override
            protected SseEmitter createEmitter() {
                return emitter;
            }
        };
    }

    private AiActivitySummaryGenerateReq request() {
        AiActivitySummaryGenerateReq req = new AiActivitySummaryGenerateReq();
        req.setConversationId(20L);
        req.setPeriod("week");
        req.setIdempotencyKey("123e4567-e89b-12d3-a456-426614174000");
        return req;
    }
}
