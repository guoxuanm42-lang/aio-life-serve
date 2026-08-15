package top.aiolife.ai.activity.service.impl;

import org.junit.jupiter.api.Test;
import top.aiolife.ai.activity.model.AiActivitySummaryGenerationClaim;
import top.aiolife.ai.activity.model.AiActivitySummaryModelResult;
import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryGenerateReq;
import top.aiolife.ai.activity.pojo.resp.AiActivitySummaryGenerateResp;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;
import top.aiolife.ai.activity.pojo.summary.ThoughtSummary;
import top.aiolife.ai.activity.service.AiActivitySummaryGenerationTaskService;
import top.aiolife.ai.activity.service.AiActivitySummaryModelService;
import top.aiolife.ai.activity.service.AiActivitySummaryPersistenceService;
import top.aiolife.ai.activity.service.AiActivitySummaryService;
import top.aiolife.ai.activity.support.AiActivitySummaryPromptBuilder;
import top.aiolife.llm.pojo.entity.ConversationEntity;
import top.aiolife.llm.service.ConversationService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 活动总结生成服务测试，验证空数据短路和一次模型调用的主流程。
 *
 * @author Ethan
 * @date 2026-08-14
 */
class AiActivitySummaryGenerateServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final Long CONVERSATION_ID = 20L;
    private static final String KEY = "123e4567-e89b-12d3-a456-426614174000";

    @Test
    void shouldSkipModelAndPersistenceWhenAllModulesAreEmpty() {
        Fixture fixture = fixture(emptyContext());

        AiActivitySummaryGenerateResp response = fixture.service.generate(USER_ID, request());

        assertEquals("当前周期内暂无可总结的活动数据", response.getContent());
        assertNull(response.getUserMessageId());
        verify(fixture.modelService, never()).generate(any(), any(), any(), any());
        verify(fixture.persistenceService, never()).persist(any());
    }

    @Test
    void shouldGenerateModelOnceAndPersistGeneratedTask() {
        AiActivitySummaryContext context = emptyContext();
        ThoughtSummary thought = new ThoughtSummary();
        thought.setNewCount(1);
        context.setThought(thought);
        Fixture fixture = fixture(context);
        AiActivitySummaryGenerationEntity task = task("PROCESSING");
        when(fixture.taskService.claim(USER_ID, CONVERSATION_ID, KEY, "week", "固定指令"))
                .thenReturn(new AiActivitySummaryGenerationClaim(task, true));
        AiActivitySummaryModelResult modelResult = AiActivitySummaryModelResult.builder()
                .content("总结结果").modelName("test-model")
                .agentCode("life_assistant").agentName("生活总助理").build();
        when(fixture.modelService.generate(USER_ID, "life_assistant", context, "固定指令"))
                .thenReturn(modelResult);
        AiActivitySummaryGenerationEntity generated = task("GENERATED");
        generated.setContent("总结结果");
        when(fixture.taskService.markGenerated(1L, modelResult)).thenReturn(generated);
        AiActivitySummaryGenerateResp persisted = AiActivitySummaryGenerateResp.builder().content("总结结果").build();
        when(fixture.persistenceService.persist(generated)).thenReturn(persisted);

        AiActivitySummaryGenerateResp response = fixture.service.generate(USER_ID, request());

        assertEquals("总结结果", response.getContent());
        verify(fixture.modelService).generate(USER_ID, "life_assistant", context, "固定指令");
        verify(fixture.persistenceService).persist(generated);
    }

    private Fixture fixture(AiActivitySummaryContext context) {
        ConversationService conversationService = mock(ConversationService.class);
        AiActivitySummaryService summaryService = mock(AiActivitySummaryService.class);
        AiActivitySummaryPromptBuilder promptBuilder = mock(AiActivitySummaryPromptBuilder.class);
        AiActivitySummaryGenerationTaskService taskService = mock(AiActivitySummaryGenerationTaskService.class);
        AiActivitySummaryModelService modelService = mock(AiActivitySummaryModelService.class);
        AiActivitySummaryPersistenceService persistenceService = mock(AiActivitySummaryPersistenceService.class);
        ConversationEntity session = new ConversationEntity();
        session.setAgentCode("life_assistant");
        when(conversationService.getOwnedSession(USER_ID, CONVERSATION_ID)).thenReturn(session);
        when(summaryService.summarize(any(), any())).thenReturn(context);
        when(promptBuilder.buildUserMessage(context)).thenReturn("固定指令");
        return new Fixture(new AiActivitySummaryGenerateServiceImpl(
                conversationService, summaryService, promptBuilder, taskService, modelService, persistenceService),
                taskService, modelService, persistenceService);
    }

    private AiActivitySummaryContext emptyContext() {
        AiActivitySummaryContext context = new AiActivitySummaryContext();
        context.setPeriod("week");
        context.setStartTime(LocalDateTime.of(2026, 8, 10, 0, 0));
        context.setEndTime(LocalDateTime.of(2026, 8, 14, 11, 0));
        return context;
    }

    private AiActivitySummaryGenerateReq request() {
        AiActivitySummaryGenerateReq req = new AiActivitySummaryGenerateReq();
        req.setPeriod("week");
        req.setConversationId(CONVERSATION_ID);
        req.setIdempotencyKey(KEY);
        return req;
    }

    private AiActivitySummaryGenerationEntity task(String status) {
        AiActivitySummaryGenerationEntity task = new AiActivitySummaryGenerationEntity();
        task.setId(1L);
        task.setUserId(USER_ID);
        task.setConversationId(CONVERSATION_ID);
        task.setIdempotencyKey(KEY);
        task.setPeriod("week");
        task.setStatus(status);
        task.setUserMessage("固定指令");
        return task;
    }

    private record Fixture(AiActivitySummaryGenerateServiceImpl service,
                           AiActivitySummaryGenerationTaskService taskService,
                           AiActivitySummaryModelService modelService,
                           AiActivitySummaryPersistenceService persistenceService) {
    }
}
