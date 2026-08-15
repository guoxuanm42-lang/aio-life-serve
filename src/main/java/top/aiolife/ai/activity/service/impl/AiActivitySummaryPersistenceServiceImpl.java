package top.aiolife.ai.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.aiolife.ai.activity.mapper.AiActivitySummaryGenerationMapper;
import top.aiolife.ai.activity.model.AiActivitySummaryGenerationStatus;
import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;
import top.aiolife.ai.activity.pojo.resp.AiActivitySummaryGenerateResp;
import top.aiolife.ai.activity.service.AiActivitySummaryPersistenceService;
import top.aiolife.llm.pojo.entity.ChatMessageEntity;
import top.aiolife.llm.service.ChatMessageService;
import top.aiolife.llm.service.ConversationService;

import java.time.LocalDateTime;

/**
 * AI 活动总结消息持久化实现，在单一事务内完成双边消息与任务状态更新。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Service
@RequiredArgsConstructor
public class AiActivitySummaryPersistenceServiceImpl implements AiActivitySummaryPersistenceService {

    private static final String SOURCE_TYPE = "activity_summary";
    private final ChatMessageService chatMessageService;
    private final ConversationService conversationService;
    private final AiActivitySummaryGenerationMapper generationMapper;

    /**
     * 原子保存活动总结用户消息、助手消息并完成幂等任务。
     *
     * @param task 已完成模型生成的幂等任务
     * @return 与落库内容一致的响应
     *
     * @author Ethan
     * @date 2026-08-14
     */
    @Override
    @Transactional
    public AiActivitySummaryGenerateResp persist(AiActivitySummaryGenerationEntity task) {
        if (task == null || !AiActivitySummaryGenerationStatus.GENERATED.name().equals(task.getStatus())) {
            throw new IllegalStateException("活动总结任务尚未完成模型生成");
        }
        ChatMessageEntity userMessage = chatMessageService.saveMessage(
                task.getUserId(), task.getConversationId(), "user", task.getUserMessage(),
                task.getModelName(), SOURCE_TYPE, task.getIdempotencyKey());
        ChatMessageEntity assistantMessage = chatMessageService.saveMessage(
                task.getUserId(), task.getConversationId(), "assistant", task.getContent(),
                task.getModelName(), SOURCE_TYPE, task.getIdempotencyKey());
        conversationService.touchSession(task.getUserId(), task.getConversationId());
        task.setUserMessageId(userMessage.getId());
        task.setAssistantMessageId(assistantMessage.getId());
        task.setStatus(AiActivitySummaryGenerationStatus.SUCCESS.name());
        task.setUpdateTime(LocalDateTime.now());
        generationMapper.updateById(task);
        return toResponse(task);
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
}
