package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.aiolife.ai.activity.mapper.AiActivitySummaryGenerationMapper;
import top.aiolife.ai.activity.model.AiActivitySummaryGenerationStatus;
import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;
import top.aiolife.ai.activity.service.AiActivitySummaryHistoryService;
import top.aiolife.ai.activity.support.AiActivitySummaryContextCodec;
import top.aiolife.llm.pojo.entity.ChatMessageEntity;
import top.aiolife.llm.pojo.resp.ChatMessageResp;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 活动总结历史装配服务实现，通过一次批量查询恢复助手报告的结构化快照。
 *
 * @author Ethan
 * @date 2026-08-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiActivitySummaryHistoryServiceImpl implements AiActivitySummaryHistoryService {

    private static final String SOURCE_TYPE = "activity_summary";
    private final AiActivitySummaryGenerationMapper generationMapper;
    private final AiActivitySummaryContextCodec contextCodec;

    /**
     * 批量装配聊天历史并仅为活动总结助手消息附加结构化数据。
     *
     * @param userId 当前用户 ID
     * @param messages 已完成归属校验并按时间排序的聊天消息
     * @return 保持原顺序的聊天历史响应列表
     *
     * @author Ethan
     * @date 2026-08-15
     */
    @Override
    public List<ChatMessageResp> assemble(Long userId, List<ChatMessageEntity> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> reportMessageIds = messages.stream()
                .filter(this::isActivitySummaryAssistantMessage)
                .map(ChatMessageEntity::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, AiActivitySummaryGenerationEntity> reportsByMessageId = loadReports(userId, reportMessageIds);
        return messages.stream()
                .map(message -> toResponse(message, reportsByMessageId.get(message.getId())))
                .toList();
    }

    private Map<Long, AiActivitySummaryGenerationEntity> loadReports(Long userId, List<Long> messageIds) {
        if (messageIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return generationMapper.selectList(new LambdaQueryWrapper<AiActivitySummaryGenerationEntity>()
                        .eq(AiActivitySummaryGenerationEntity::getUserId, userId)
                        .eq(AiActivitySummaryGenerationEntity::getStatus,
                                AiActivitySummaryGenerationStatus.SUCCESS.name())
                        .in(AiActivitySummaryGenerationEntity::getAssistantMessageId, messageIds))
                .stream()
                .collect(Collectors.toMap(AiActivitySummaryGenerationEntity::getAssistantMessageId,
                        Function.identity(), (first, ignored) -> first));
    }

    private boolean isActivitySummaryAssistantMessage(ChatMessageEntity message) {
        return "assistant".equals(message.getRole()) && SOURCE_TYPE.equals(message.getSourceType());
    }

    private ChatMessageResp toResponse(ChatMessageEntity message, AiActivitySummaryGenerationEntity report) {
        return ChatMessageResp.builder()
                .id(message.getId())
                .userId(message.getUserId())
                .conversationId(message.getConversationId())
                .role(message.getRole())
                .content(message.getContent())
                .modelName(message.getModelName())
                .sourceType(message.getSourceType())
                .idempotencyKey(message.getIdempotencyKey())
                .activitySummary(deserializeSafely(report))
                .createUser(message.getCreateUser())
                .createTime(message.getCreateTime())
                .updateUser(message.getUpdateUser())
                .updateTime(message.getUpdateTime())
                .isDeleted(message.getIsDeleted())
                .build();
    }

    private AiActivitySummaryContext deserializeSafely(AiActivitySummaryGenerationEntity report) {
        if (report == null || report.getContextJson() == null) {
            return null;
        }
        try {
            return contextCodec.deserialize(report.getContextJson());
        } catch (IllegalStateException exception) {
            log.warn("Ignoring invalid activity summary snapshot for task {}", report.getId(), exception);
            return null;
        }
    }
}
