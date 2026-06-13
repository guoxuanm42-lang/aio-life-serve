package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.record.mapper.IRelaEventMapper;
import top.aiolife.record.mapper.IThoughtActionDetailMapper;
import top.aiolife.record.mapper.IThoughtEmotionDetailMapper;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.mapper.IThoughtReflectionDetailMapper;
import top.aiolife.record.mapper.IThoughtStatusLogMapper;
import top.aiolife.record.pojo.entity.ThoughtActionDetailEntity;
import top.aiolife.record.pojo.entity.ThoughtEmotionDetailEntity;
import top.aiolife.record.pojo.entity.ThoughtEntity;
import top.aiolife.record.pojo.entity.ThoughtReflectionDetailEntity;
import top.aiolife.record.pojo.entity.ThoughtRelaEventEntity;
import top.aiolife.record.pojo.entity.ThoughtStatusLogEntity;
import top.aiolife.record.pojo.req.CommonReq;
import top.aiolife.record.pojo.req.ThoughtActionDetailReq;
import top.aiolife.record.pojo.req.ThoughtEmotionDetailReq;
import top.aiolife.record.pojo.req.ThoughtReflectionDetailReq;
import top.aiolife.record.pojo.req.ThoughtSaveEventReq;
import top.aiolife.record.pojo.req.ThoughtSaveReq;
import top.aiolife.record.pojo.vo.ThoughtDetailVO;
import top.aiolife.record.service.IThoughtService;
import top.aiolife.record.util.RedisUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 闪念服务实现，负责主记录、事件流和结构化详情的事务化读写。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Service
@RequiredArgsConstructor
public class ThoughtServiceImpl implements IThoughtService {

    private static final Set<String> ALLOWED_THEME_KEYS = Set.of(
            "blue", "cyan", "green", "purple", "pink", "orange", "teal", "indigo"
    );

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "pending", "ongoing", "done", "shelved", "archived"
    );

    private static final Set<String> ALLOWED_THOUGHT_TYPES = Set.of(
            "action", "emotion", "reflection"
    );

    private static final Set<String> ALLOWED_VALUE_LEVELS = Set.of("normal", "valuable", "high");

    private static final Set<String> ALLOWED_ARCHIVE_TYPES = Set.of(
            "experience", "lesson", "method", "inspiration", "decision"
    );

    private static final Set<String> ALLOWED_LESSON_TYPES = Set.of(
            "experience", "lesson", "method", "decision"
    );

    private static final Set<String> ALLOWED_EMOTION_TYPES = Set.of(
            "sad", "angry", "anxious", "stress", "happy", "excited", "moved", "inspired"
    );

    private static final Set<String> ALLOWED_SHELVE_REASON_TAGS = Set.of(
            "unrealistic", "no_time", "low_value", "blocked", "duplicate", "other"
    );

    private static final Set<String> ALLOWED_RESTART_POLICIES = Set.of("no", "later", "conditional");

    private static final long IDEMPOTENCY_TTL_SECONDS = TimeUnit.HOURS.toSeconds(24);

    private final IThoughtMapper thoughtMapper;
    private final IRelaEventMapper relaEventMapper;
    private final IThoughtActionDetailMapper actionDetailMapper;
    private final IThoughtEmotionDetailMapper emotionDetailMapper;
    private final IThoughtReflectionDetailMapper reflectionDetailMapper;
    private final IThoughtStatusLogMapper statusLogMapper;
    private final RedisUtil redisUtil;

    /**
     * 保存闪念主记录、事件流和当前类型详情。
     *
     * @param req 闪念保存请求
     * @param userId 当前登录用户 ID
     * @param idempotencyKey 幂等键
     * @return 统一返回结构，data 表示是否保存成功
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Boolean> save(ThoughtSaveReq req, long userId, String idempotencyKey) {
        String idempotencyRedisKey = buildIdempotencyKey(userId, idempotencyKey);
        if (idempotencyRedisKey != null) {
            Boolean locked = redisUtil.setIfAbsent(idempotencyRedisKey, "1", IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(locked)) {
                return ApiResponse.success(true);
            }
        }

        try {
            ThoughtEntity entity = buildThoughtEntity(req, userId, false);
            entity.fillCreateCommonField(userId);
            thoughtMapper.insert(entity);
            replaceEvents(entity.getId(), userId, req.getEvents());
            saveCurrentDetail(entity.getId(), userId, entity.getThoughtType(), req);
            recordStatusLog(entity.getId(), userId, entity.getThoughtType(), null, entity.getStatus(), req.getChangeReason());
            return ApiResponse.success(true);
        } catch (Exception e) {
            if (idempotencyRedisKey != null) {
                redisUtil.delete(idempotencyRedisKey);
            }
            throw e;
        }
    }

    /**
     * 更新闪念主记录、事件流和当前类型详情。
     *
     * @param req 闪念更新请求
     * @param userId 当前登录用户 ID
     * @return 统一返回结构，data 表示是否更新成功
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Boolean> update(ThoughtSaveReq req, long userId) {
        if (req == null || req.getId() == null) {
            return ApiResponse.error("闪念 ID 不能为空");
        }
        ThoughtEntity exist = getOwnedThought(req.getId(), userId);
        if (exist == null) {
            return ApiResponse.error("无权操作或记录不存在");
        }

        String oldStatus = normalizeStatus(exist.getStatus());
        ThoughtEntity entity = buildThoughtEntity(req, userId, true);
        entity.setId(req.getId());
        entity.fillUpdateCommonField(userId);
        LambdaQueryWrapper<ThoughtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtEntity::getId, req.getId());
        wrapper.eq(ThoughtEntity::getUserId, userId);
        wrapper.eq(ThoughtEntity::getIsDeleted, 0);
        int rows = thoughtMapper.update(entity, wrapper);
        if (rows <= 0) {
            return ApiResponse.error("无权操作或记录不存在");
        }
        replaceEvents(req.getId(), userId, req.getEvents());
        saveCurrentDetail(req.getId(), userId, entity.getThoughtType(), req);
        if (!Objects.equals(oldStatus, entity.getStatus())) {
            recordStatusLog(req.getId(), userId, entity.getThoughtType(), oldStatus, entity.getStatus(), req.getChangeReason());
        }
        return ApiResponse.success(true);
    }

    /**
     * 查询闪念详情。
     *
     * @param id 闪念 ID
     * @param userId 当前登录用户 ID
     * @return 闪念详情
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @Override
    public ThoughtDetailVO detail(Long id, long userId) {
        ThoughtEntity thought = getOwnedThought(id, userId);
        if (thought == null) {
            return null;
        }
        List<ThoughtRelaEventEntity> events = listEvents(id, userId);
        thought.setEvents(events);

        ThoughtDetailVO vo = new ThoughtDetailVO();
        vo.setThought(thought);
        vo.setEvents(events);
        vo.setActionDetail(getActionDetail(id, userId));
        vo.setEmotionDetail(getEmotionDetail(id, userId));
        vo.setReflectionDetail(getReflectionDetail(id, userId));
        vo.setStatusLogs(listStatusLogs(id, userId));
        return vo;
    }

    /**
     * 批量删除闪念及关联数据。
     *
     * @param req 批量删除请求
     * @param userId 当前登录用户 ID
     * @return 统一返回结构，data 表示是否删除成功
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Boolean> batchDelete(CommonReq req, long userId) {
        if (req == null || req.getIdList() == null || req.getIdList().isEmpty()) {
            return ApiResponse.success(true);
        }
        List<Long> ids = req.getIdList().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(this::isLong)
                .map(Long::valueOf)
                .toList();
        if (ids.isEmpty()) {
            return ApiResponse.success(true);
        }

        LambdaQueryWrapper<ThoughtEntity> ownedQuery = new LambdaQueryWrapper<>();
        ownedQuery.select(ThoughtEntity::getId);
        ownedQuery.eq(ThoughtEntity::getUserId, userId);
        ownedQuery.in(ThoughtEntity::getId, ids);
        List<Long> ownedIds = thoughtMapper.selectList(ownedQuery).stream()
                .map(ThoughtEntity::getId)
                .toList();
        if (ownedIds.isEmpty()) {
            return ApiResponse.success(true);
        }

        LambdaQueryWrapper<ThoughtEntity> thoughtWrapper = new LambdaQueryWrapper<>();
        thoughtWrapper.eq(ThoughtEntity::getUserId, userId);
        thoughtWrapper.in(ThoughtEntity::getId, ownedIds);
        thoughtMapper.delete(thoughtWrapper);

        LambdaQueryWrapper<ThoughtRelaEventEntity> eventWrapper = new LambdaQueryWrapper<>();
        eventWrapper.in(ThoughtRelaEventEntity::getThoughtId, ownedIds);
        eventWrapper.eq(ThoughtRelaEventEntity::getIsDeleted, 0);
        relaEventMapper.delete(eventWrapper);

        LambdaQueryWrapper<ThoughtActionDetailEntity> actionWrapper = new LambdaQueryWrapper<>();
        actionWrapper.eq(ThoughtActionDetailEntity::getUserId, userId);
        actionWrapper.in(ThoughtActionDetailEntity::getThoughtId, ownedIds);
        actionDetailMapper.delete(actionWrapper);

        LambdaQueryWrapper<ThoughtEmotionDetailEntity> emotionWrapper = new LambdaQueryWrapper<>();
        emotionWrapper.eq(ThoughtEmotionDetailEntity::getUserId, userId);
        emotionWrapper.in(ThoughtEmotionDetailEntity::getThoughtId, ownedIds);
        emotionDetailMapper.delete(emotionWrapper);

        LambdaQueryWrapper<ThoughtReflectionDetailEntity> reflectionWrapper = new LambdaQueryWrapper<>();
        reflectionWrapper.eq(ThoughtReflectionDetailEntity::getUserId, userId);
        reflectionWrapper.in(ThoughtReflectionDetailEntity::getThoughtId, ownedIds);
        reflectionDetailMapper.delete(reflectionWrapper);

        LambdaQueryWrapper<ThoughtStatusLogEntity> statusLogWrapper = new LambdaQueryWrapper<>();
        statusLogWrapper.eq(ThoughtStatusLogEntity::getUserId, userId);
        statusLogWrapper.in(ThoughtStatusLogEntity::getThoughtId, ownedIds);
        statusLogMapper.delete(statusLogWrapper);
        return ApiResponse.success(true);
    }

    private ThoughtEntity buildThoughtEntity(ThoughtSaveReq req, long userId, boolean update) {
        if (req == null) {
            throw new IllegalArgumentException("闪念不能为空");
        }
        String subject = req.getSubject() == null ? null : req.getSubject().trim();
        if (subject == null || subject.isBlank()) {
            String content = req.getContent() == null ? "" : req.getContent().trim();
            String firstLine = content.split("\\R", 2)[0].trim();
            subject = firstLine.isBlank() ? null : (firstLine.length() > 60 ? firstLine.substring(0, 60) : firstLine);
        }
        if (subject == null) {
            throw new IllegalArgumentException("主题内容不能为空");
        }

        ThoughtEntity entity = new ThoughtEntity();
        entity.setUserId(update ? null : userId);
        entity.setSubject(subject);
        entity.setContent(req.getContent());

        String themeKey = req.getThemeKey();
        if (themeKey != null && ALLOWED_THEME_KEYS.contains(themeKey)) {
            entity.setThemeKey(themeKey);
        }

        String normalizedStatus = normalizeStatus(req.getStatus());
        entity.setStatus(Objects.requireNonNullElse(normalizedStatus, "pending"));
        entity.setThoughtType(normalizeThoughtTypeOrDefault(req.getThoughtType()));
        return entity;
    }

    private void replaceEvents(Long thoughtId, long userId, List<ThoughtSaveEventReq> events) {
        LambdaQueryWrapper<ThoughtRelaEventEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtRelaEventEntity::getThoughtId, thoughtId);
        wrapper.eq(ThoughtRelaEventEntity::getIsDeleted, 0);
        relaEventMapper.delete(wrapper);

        if (events == null) {
            return;
        }
        for (ThoughtSaveEventReq eventReq : events) {
            if (eventReq == null || !StringUtils.hasText(eventReq.getContent())) {
                continue;
            }
            ThoughtRelaEventEntity eventEntity = new ThoughtRelaEventEntity();
            eventEntity.setThoughtId(thoughtId);
            eventEntity.setContent(eventReq.getContent().trim());
            eventEntity.fillCreateCommonField(userId);
            relaEventMapper.insert(eventEntity);
        }
    }

    private void saveCurrentDetail(Long thoughtId, long userId, String thoughtType, ThoughtSaveReq req) {
        if ("emotion".equals(thoughtType)) {
            saveEmotionDetail(thoughtId, userId, req.getEmotionDetail());
            return;
        }
        if ("reflection".equals(thoughtType)) {
            saveReflectionDetail(thoughtId, userId, req.getReflectionDetail());
            return;
        }
        saveActionDetail(thoughtId, userId, req.getActionDetail());
    }

    private void saveActionDetail(Long thoughtId, long userId, ThoughtActionDetailReq req) {
        if (req == null) {
            return;
        }
        ThoughtActionDetailEntity entity = new ThoughtActionDetailEntity();
        entity.setResultSummary(normalizeText(req.getResultSummary()));
        entity.setReflection(normalizeText(req.getReflection()));
        entity.setNextAction(normalizeText(req.getNextAction()));
        entity.setShelveReason(normalizeText(req.getShelveReason()));
        entity.setShelveReasonTag(normalizeOption(req.getShelveReasonTag(), ALLOWED_SHELVE_REASON_TAGS));
        entity.setRestartPolicy(normalizeOption(req.getRestartPolicy(), ALLOWED_RESTART_POLICIES));
        entity.setArchiveReason(normalizeText(req.getArchiveReason()));
        entity.setValueLevel(normalizeOption(req.getValueLevel(), ALLOWED_VALUE_LEVELS));
        entity.setArchiveType(normalizeOption(req.getArchiveType(), ALLOWED_ARCHIVE_TYPES));

        ThoughtActionDetailEntity exist = getActionDetail(thoughtId, userId);
        upsertActionDetail(thoughtId, userId, entity, exist);
    }

    private void saveEmotionDetail(Long thoughtId, long userId, ThoughtEmotionDetailReq req) {
        if (req == null) {
            return;
        }
        ThoughtEmotionDetailEntity entity = new ThoughtEmotionDetailEntity();
        entity.setEmotionType(normalizeOption(req.getEmotionType(), ALLOWED_EMOTION_TYPES));
        Integer intensity = req.getEmotionIntensity();
        entity.setEmotionIntensity(intensity == null ? null : Math.max(1, Math.min(5, intensity)));
        entity.setEmotionTrigger(normalizeText(req.getEmotionTrigger()));
        entity.setEmotionNeed(normalizeText(req.getEmotionNeed()));
        entity.setCopingAction(normalizeText(req.getCopingAction()));
        entity.setReflectionSummary(normalizeText(req.getReflectionSummary()));
        entity.setIgnoredReason(normalizeText(req.getIgnoredReason()));

        ThoughtEmotionDetailEntity exist = getEmotionDetail(thoughtId, userId);
        upsertEmotionDetail(thoughtId, userId, entity, exist);
    }

    private void saveReflectionDetail(Long thoughtId, long userId, ThoughtReflectionDetailReq req) {
        if (req == null) {
            return;
        }
        ThoughtReflectionDetailEntity entity = new ThoughtReflectionDetailEntity();
        entity.setReflectionSummary(normalizeText(req.getReflectionSummary()));
        entity.setLessonType(normalizeOption(req.getLessonType(), ALLOWED_LESSON_TYPES));
        entity.setArchiveType(normalizeOption(req.getArchiveType(), ALLOWED_ARCHIVE_TYPES));
        entity.setValueLevel(normalizeOption(req.getValueLevel(), ALLOWED_VALUE_LEVELS));
        entity.setImprovementAction(normalizeText(req.getImprovementAction()));
        entity.setRelatedProject(normalizeText(req.getRelatedProject()));
        entity.setTags(normalizeText(req.getTags()));

        ThoughtReflectionDetailEntity exist = getReflectionDetail(thoughtId, userId);
        upsertReflectionDetail(thoughtId, userId, entity, exist);
    }

    private void upsertActionDetail(
            Long thoughtId, long userId, ThoughtActionDetailEntity entity, ThoughtActionDetailEntity exist) {
        entity.setThoughtId(thoughtId);
        entity.setUserId(userId);
        if (exist == null) {
            entity.fillCreateCommonField(userId);
            actionDetailMapper.insert(entity);
            return;
        }
        entity.setId(exist.getId());
        entity.fillUpdateCommonField(userId);
        actionDetailMapper.updateById(entity);
    }

    private void upsertEmotionDetail(
            Long thoughtId, long userId, ThoughtEmotionDetailEntity entity, ThoughtEmotionDetailEntity exist) {
        entity.setThoughtId(thoughtId);
        entity.setUserId(userId);
        if (exist == null) {
            entity.fillCreateCommonField(userId);
            emotionDetailMapper.insert(entity);
            return;
        }
        entity.setId(exist.getId());
        entity.fillUpdateCommonField(userId);
        emotionDetailMapper.updateById(entity);
    }

    private void upsertReflectionDetail(
            Long thoughtId, long userId, ThoughtReflectionDetailEntity entity, ThoughtReflectionDetailEntity exist) {
        entity.setThoughtId(thoughtId);
        entity.setUserId(userId);
        if (exist == null) {
            entity.fillCreateCommonField(userId);
            reflectionDetailMapper.insert(entity);
            return;
        }
        entity.setId(exist.getId());
        entity.fillUpdateCommonField(userId);
        reflectionDetailMapper.updateById(entity);
    }

    private ThoughtEntity getOwnedThought(Long id, long userId) {
        if (id == null) {
            return null;
        }
        LambdaQueryWrapper<ThoughtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtEntity::getId, id);
        wrapper.eq(ThoughtEntity::getUserId, userId);
        wrapper.eq(ThoughtEntity::getIsDeleted, 0);
        return thoughtMapper.selectOne(wrapper);
    }

    private List<ThoughtRelaEventEntity> listEvents(Long thoughtId, long userId) {
        if (thoughtId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ThoughtRelaEventEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtRelaEventEntity::getThoughtId, thoughtId);
        wrapper.eq(ThoughtRelaEventEntity::getIsDeleted, 0);
        wrapper.orderByAsc(ThoughtRelaEventEntity::getCreateTime);
        return relaEventMapper.selectList(wrapper);
    }

    private ThoughtActionDetailEntity getActionDetail(Long thoughtId, long userId) {
        LambdaQueryWrapper<ThoughtActionDetailEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtActionDetailEntity::getThoughtId, thoughtId);
        wrapper.eq(ThoughtActionDetailEntity::getUserId, userId);
        wrapper.eq(ThoughtActionDetailEntity::getIsDeleted, 0);
        return actionDetailMapper.selectOne(wrapper);
    }

    private ThoughtEmotionDetailEntity getEmotionDetail(Long thoughtId, long userId) {
        LambdaQueryWrapper<ThoughtEmotionDetailEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtEmotionDetailEntity::getThoughtId, thoughtId);
        wrapper.eq(ThoughtEmotionDetailEntity::getUserId, userId);
        wrapper.eq(ThoughtEmotionDetailEntity::getIsDeleted, 0);
        return emotionDetailMapper.selectOne(wrapper);
    }

    private ThoughtReflectionDetailEntity getReflectionDetail(Long thoughtId, long userId) {
        LambdaQueryWrapper<ThoughtReflectionDetailEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtReflectionDetailEntity::getThoughtId, thoughtId);
        wrapper.eq(ThoughtReflectionDetailEntity::getUserId, userId);
        wrapper.eq(ThoughtReflectionDetailEntity::getIsDeleted, 0);
        return reflectionDetailMapper.selectOne(wrapper);
    }

    private List<ThoughtStatusLogEntity> listStatusLogs(Long thoughtId, long userId) {
        if (thoughtId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ThoughtStatusLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtStatusLogEntity::getThoughtId, thoughtId);
        wrapper.eq(ThoughtStatusLogEntity::getUserId, userId);
        wrapper.eq(ThoughtStatusLogEntity::getIsDeleted, 0);
        wrapper.orderByAsc(ThoughtStatusLogEntity::getCreateTime);
        return statusLogMapper.selectList(wrapper);
    }

    private void recordStatusLog(
            Long thoughtId,
            long userId,
            String thoughtType,
            String fromStatus,
            String toStatus,
            String changeReason) {
        if (thoughtId == null || !StringUtils.hasText(toStatus)) {
            return;
        }
        ThoughtStatusLogEntity log = new ThoughtStatusLogEntity();
        log.setThoughtId(thoughtId);
        log.setUserId(userId);
        log.setThoughtType(normalizeThoughtTypeOrDefault(thoughtType));
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setChangeReason(normalizeText(changeReason));
        log.fillCreateCommonField(userId);
        statusLogMapper.insert(log);
    }

    private String buildIdempotencyKey(long userId, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return null;
        }
        return "mcp:idemp:thought_save:" + userId + ":" + idempotencyKey.trim();
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        String trimmed = status.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        String lower = trimmed.toLowerCase();
        if (ALLOWED_STATUSES.contains(lower)) {
            return lower;
        }
        return switch (trimmed) {
            case "待处理" -> "pending";
            case "进行中" -> "ongoing";
            case "已完成" -> "done";
            case "已搁置" -> "shelved";
            case "已归档" -> "archived";
            default -> null;
        };
    }

    private String normalizeThoughtTypeOrDefault(String thoughtType) {
        if (!StringUtils.hasText(thoughtType)) {
            return "action";
        }
        String normalized = thoughtType.trim().toLowerCase();
        return ALLOWED_THOUGHT_TYPES.contains(normalized) ? normalized : "action";
    }

    private String normalizeText(String text) {
        return text == null ? null : text.trim();
    }

    private String normalizeOption(String value, Set<String> allowedValues) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        return allowedValues.contains(normalized) ? normalized : null;
    }

    private boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
