package top.aiolife.ai.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.model.AiActivitySummaryGenerationClaim;
import top.aiolife.ai.activity.model.AiActivitySummaryGenerationStatus;
import top.aiolife.ai.activity.model.AiActivitySummaryModelResult;
import top.aiolife.ai.activity.model.AiActivitySummaryPeriod;
import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryGenerateReq;
import top.aiolife.ai.activity.pojo.req.AiActivitySummaryReq;
import top.aiolife.ai.activity.pojo.resp.AiActivitySummaryGenerateResp;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;
import top.aiolife.ai.activity.service.AiActivitySummaryGenerateService;
import top.aiolife.ai.activity.service.AiActivitySummaryGenerationTaskService;
import top.aiolife.ai.activity.service.AiActivitySummaryModelService;
import top.aiolife.ai.activity.service.AiActivitySummaryPersistenceService;
import top.aiolife.ai.activity.service.AiActivitySummaryService;
import top.aiolife.ai.activity.support.AiActivitySummaryPromptBuilder;
import top.aiolife.llm.pojo.entity.ConversationEntity;
import top.aiolife.llm.service.ConversationService;

import java.util.UUID;

/**
 * AI 活动总结生成服务实现，以幂等状态机串联统计、一次模型调用和原子消息保存。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Service
@RequiredArgsConstructor
public class AiActivitySummaryGenerateServiceImpl implements AiActivitySummaryGenerateService {

    private static final String DEFAULT_AGENT_CODE = "life_assistant";
    private static final String EMPTY_CONTENT = "当前周期内暂无可总结的活动数据";
    private final ConversationService conversationService;
    private final AiActivitySummaryService summaryService;
    private final AiActivitySummaryPromptBuilder promptBuilder;
    private final AiActivitySummaryGenerationTaskService taskService;
    private final AiActivitySummaryModelService modelService;
    private final AiActivitySummaryPersistenceService persistenceService;

    /**
     * 生成并保存当前用户指定周期的活动总结。
     *
     * @param userId 当前用户 ID
     * @param req 活动总结生成请求
     * @return 生成内容和落库消息信息；空活动时消息 ID 为空且不调用模型
     *
     * @author Ethan
     * @date 2026-08-14
     */
    @Override
    public AiActivitySummaryGenerateResp generate(Long userId, AiActivitySummaryGenerateReq req) {
        ValidatedRequest validated = validate(userId, req);
        ConversationEntity session = conversationService.getOwnedSession(userId, validated.conversationId());
        AiActivitySummaryGenerationEntity existing = taskService.find(
                userId, validated.conversationId(), validated.idempotencyKey());
        if (existing != null) {
            validateExistingPeriod(existing, validated.period());
            if (AiActivitySummaryGenerationStatus.SUCCESS.name().equals(existing.getStatus())) {
                return toResponse(existing);
            }
        }

        AiActivitySummaryReq summaryReq = new AiActivitySummaryReq();
        summaryReq.setPeriod(validated.period());
        summaryReq.setConversationId(validated.conversationId());
        AiActivitySummaryContext context = summaryService.summarize(userId, summaryReq);
        String userMessage = promptBuilder.buildUserMessage(context);
        String agentCode = StringUtils.hasText(session.getAgentCode())
                ? session.getAgentCode().trim() : DEFAULT_AGENT_CODE;
        if (isEmpty(context)) {
            return AiActivitySummaryGenerateResp.builder()
                    .conversationId(validated.conversationId())
                    .period(validated.period())
                    .userMessage(userMessage)
                    .content(EMPTY_CONTENT)
                    .agentCode(agentCode)
                    .build();
        }

        AiActivitySummaryGenerationClaim claim = taskService.claim(
                userId, validated.conversationId(), validated.idempotencyKey(), validated.period(), userMessage);
        AiActivitySummaryGenerationEntity task = claim.getTask();
        if (claim.isModelGenerationRequired()) {
            try {
                AiActivitySummaryModelResult modelResult = modelService.generate(userId, agentCode, context, userMessage);
                task = taskService.markGenerated(task.getId(), modelResult);
            } catch (RuntimeException exception) {
                taskService.markFailed(task.getId(), exception.getMessage());
                throw exception;
            }
        }
        if (AiActivitySummaryGenerationStatus.SUCCESS.name().equals(task.getStatus())) {
            return toResponse(task);
        }
        return persistenceService.persist(task);
    }

    private ValidatedRequest validate(Long userId, AiActivitySummaryGenerateReq req) {
        if (userId == null) {
            throw new IllegalArgumentException("用户 ID 不能为空");
        }
        if (req == null) {
            throw new IllegalArgumentException("活动总结生成请求不能为空");
        }
        String period = AiActivitySummaryPeriod.fromValue(req.getPeriod()).value();
        if (req.getConversationId() == null) {
            throw new IllegalArgumentException("会话 ID 不能为空");
        }
        String key = req.getIdempotencyKey();
        if (!StringUtils.hasText(key) || !key.equals(key.trim()) || key.length() > 64) {
            throw new IllegalArgumentException("幂等键必须为有效 UUID");
        }
        try {
            if (!UUID.fromString(key).toString().equalsIgnoreCase(key)) {
                throw new IllegalArgumentException("幂等键必须为有效 UUID");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("幂等键必须为有效 UUID");
        }
        return new ValidatedRequest(period, req.getConversationId(), key);
    }

    private void validateExistingPeriod(AiActivitySummaryGenerationEntity task, String period) {
        if (!period.equals(task.getPeriod())) {
            throw new IllegalArgumentException("同一幂等键不能用于不同统计周期");
        }
    }

    private boolean isEmpty(AiActivitySummaryContext context) {
        return context.getTimeRecord() == null && context.getThought() == null && context.getFood() == null
                && context.getTodo() == null && context.getProblem() == null && context.getNote() == null
                && context.getAlbum() == null && context.getArticle() == null && context.getMcp() == null;
    }

    private AiActivitySummaryGenerateResp toResponse(AiActivitySummaryGenerationEntity task) {
        return AiActivitySummaryGenerateResp.builder()
                .conversationId(task.getConversationId())
                .userMessageId(task.getUserMessageId())
                .assistantMessageId(task.getAssistantMessageId())
                .period(task.getPeriod())
                .userMessage(task.getUserMessage())
                .content(task.getContent())
                .modelName(task.getModelName())
                .agentCode(task.getAgentCode())
                .agentName(task.getAgentName())
                .build();
    }

    private record ValidatedRequest(String period, Long conversationId, String idempotencyKey) {
    }
}
