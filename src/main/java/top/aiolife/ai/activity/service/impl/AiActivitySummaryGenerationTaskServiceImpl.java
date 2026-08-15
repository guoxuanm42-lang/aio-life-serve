package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.aiolife.ai.activity.mapper.AiActivitySummaryGenerationMapper;
import top.aiolife.ai.activity.model.AiActivitySummaryGenerationClaim;
import top.aiolife.ai.activity.model.AiActivitySummaryGenerationStatus;
import top.aiolife.ai.activity.model.AiActivitySummaryModelResult;
import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;
import top.aiolife.ai.activity.service.AiActivitySummaryGenerationTaskService;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AI 活动总结幂等任务服务实现，通过唯一键和条件更新防止重复模型调用。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Service
@RequiredArgsConstructor
public class AiActivitySummaryGenerationTaskServiceImpl implements AiActivitySummaryGenerationTaskService {

    private final AiActivitySummaryGenerationMapper mapper;

    /**
     * 查询指定幂等任务。
     *
     * @param userId 当前用户 ID
     * @param conversationId 会话 ID
     * @param idempotencyKey 幂等键
     * @return 已有任务，不存在时返回 null
     *
     * @author Ethan
     * @date 2026-08-14
     */
    @Override
    public AiActivitySummaryGenerationEntity find(Long userId, Long conversationId, String idempotencyKey) {
        return mapper.selectOne(new LambdaQueryWrapper<AiActivitySummaryGenerationEntity>()
                .eq(AiActivitySummaryGenerationEntity::getUserId, userId)
                .eq(AiActivitySummaryGenerationEntity::getConversationId, conversationId)
                .eq(AiActivitySummaryGenerationEntity::getIdempotencyKey, idempotencyKey));
    }

    /**
     * 创建、恢复或读取幂等生成任务。
     *
     * @param userId 当前用户 ID
     * @param conversationId 会话 ID
     * @param idempotencyKey 幂等键
     * @param period 标准统计周期
     * @param userMessage 固定用户指令
     * @return 任务认领结果
     *
     * @author Ethan
     * @date 2026-08-14
     */
    @Override
    public AiActivitySummaryGenerationClaim claim(Long userId, Long conversationId, String idempotencyKey,
                                                  String period, String userMessage) {
        AiActivitySummaryGenerationEntity existing = find(userId, conversationId, idempotencyKey);
        if (existing == null) {
            AiActivitySummaryGenerationEntity created = new AiActivitySummaryGenerationEntity();
            created.setUserId(userId);
            created.setConversationId(conversationId);
            created.setIdempotencyKey(idempotencyKey);
            created.setPeriod(period);
            created.setStatus(AiActivitySummaryGenerationStatus.PROCESSING.name());
            created.setUserMessage(userMessage);
            created.setCreateTime(LocalDateTime.now());
            created.setUpdateTime(created.getCreateTime());
            try {
                mapper.insert(created);
                return new AiActivitySummaryGenerationClaim(created, true);
            } catch (DuplicateKeyException exception) {
                existing = requireTask(userId, conversationId, idempotencyKey);
            }
        }
        validatePeriod(existing, period);
        AiActivitySummaryGenerationStatus status = AiActivitySummaryGenerationStatus.valueOf(existing.getStatus());
        if (status == AiActivitySummaryGenerationStatus.SUCCESS
                || status == AiActivitySummaryGenerationStatus.GENERATED) {
            return new AiActivitySummaryGenerationClaim(existing, false);
        }
        if (status == AiActivitySummaryGenerationStatus.PROCESSING) {
            throw new IllegalStateException("活动总结正在生成中，请稍后重试");
        }
        int updated = mapper.update(null, new LambdaUpdateWrapper<AiActivitySummaryGenerationEntity>()
                .eq(AiActivitySummaryGenerationEntity::getId, existing.getId())
                .eq(AiActivitySummaryGenerationEntity::getStatus, AiActivitySummaryGenerationStatus.FAILED.name())
                .set(AiActivitySummaryGenerationEntity::getStatus, AiActivitySummaryGenerationStatus.PROCESSING.name())
                .set(AiActivitySummaryGenerationEntity::getErrorMessage, null)
                .set(AiActivitySummaryGenerationEntity::getUpdateTime, LocalDateTime.now()));
        if (updated != 1) {
            throw new IllegalStateException("活动总结正在生成中，请稍后重试");
        }
        existing.setStatus(AiActivitySummaryGenerationStatus.PROCESSING.name());
        existing.setErrorMessage(null);
        return new AiActivitySummaryGenerationClaim(existing, true);
    }

    /**
     * 保存模型输出并将任务推进至 GENERATED。
     *
     * @param taskId 任务 ID
     * @param result 模型生成结果
     * @return 更新后的任务
     *
     * @author Ethan
     * @date 2026-08-14
     */
    @Override
    @Transactional
    public AiActivitySummaryGenerationEntity markGenerated(Long taskId, AiActivitySummaryModelResult result) {
        AiActivitySummaryGenerationEntity task = mapper.selectById(taskId);
        task.setStatus(AiActivitySummaryGenerationStatus.GENERATED.name());
        task.setContent(result.getContent());
        task.setModelName(result.getModelName());
        task.setAgentCode(result.getAgentCode());
        task.setAgentName(result.getAgentName());
        task.setErrorMessage(null);
        task.setUpdateTime(LocalDateTime.now());
        mapper.updateById(task);
        return task;
    }

    /**
     * 记录模型生成失败状态，供相同幂等键重新认领。
     *
     * @param taskId 任务 ID
     * @param errorMessage 精简错误信息
     *
     * @author Ethan
     * @date 2026-08-14
     */
    @Override
    @Transactional
    public void markFailed(Long taskId, String errorMessage) {
        mapper.update(null, new LambdaUpdateWrapper<AiActivitySummaryGenerationEntity>()
                .eq(AiActivitySummaryGenerationEntity::getId, taskId)
                .set(AiActivitySummaryGenerationEntity::getStatus, AiActivitySummaryGenerationStatus.FAILED.name())
                .set(AiActivitySummaryGenerationEntity::getErrorMessage,
                        errorMessage == null ? "模型生成失败" : errorMessage.substring(0, Math.min(500, errorMessage.length())))
                .set(AiActivitySummaryGenerationEntity::getUpdateTime, LocalDateTime.now()));
    }

    private AiActivitySummaryGenerationEntity requireTask(Long userId, Long conversationId, String idempotencyKey) {
        AiActivitySummaryGenerationEntity task = find(userId, conversationId, idempotencyKey);
        if (task == null) {
            throw new IllegalStateException("活动总结任务创建失败");
        }
        return task;
    }

    private void validatePeriod(AiActivitySummaryGenerationEntity task, String period) {
        if (!Objects.equals(task.getPeriod(), period)) {
            throw new IllegalArgumentException("同一幂等键不能用于不同统计周期");
        }
    }
}
