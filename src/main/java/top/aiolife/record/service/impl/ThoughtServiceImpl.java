package top.aiolife.record.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.record.mapper.IRelaEventMapper;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.pojo.entity.ThoughtEntity;
import top.aiolife.record.pojo.entity.ThoughtRelaEventEntity;
import top.aiolife.record.pojo.req.ThoughtSaveEventReq;
import top.aiolife.record.pojo.req.ThoughtSaveReq;
import top.aiolife.record.service.IThoughtService;
import top.aiolife.record.util.RedisUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 闪念（思考）服务实现
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Service
public class ThoughtServiceImpl implements IThoughtService {

    private static final Set<String> ALLOWED_THEME_KEYS = Set.of(
            "blue", "cyan", "green", "purple", "pink", "orange", "teal", "indigo"
    );

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "pending", "ongoing", "done", "shelved", "archived"
    );

    private static final long IDEMPOTENCY_TTL_SECONDS = TimeUnit.HOURS.toSeconds(24);

    private final IThoughtMapper thoughtMapper;
    private final IRelaEventMapper relaEventMapper;
    private final RedisUtil redisUtil;

    public ThoughtServiceImpl(IThoughtMapper thoughtMapper, IRelaEventMapper relaEventMapper, RedisUtil redisUtil) {
        this.thoughtMapper = thoughtMapper;
        this.relaEventMapper = relaEventMapper;
        this.redisUtil = redisUtil;
    }

    @Override
    public ApiResponse<Boolean> save(ThoughtSaveReq req, long userId, String idempotencyKey) {
        String idempotencyRedisKey = buildIdempotencyKey(userId, idempotencyKey);
        if (idempotencyRedisKey != null) {
            Boolean locked = redisUtil.setIfAbsent(idempotencyRedisKey, "1", IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(locked)) {
                return ApiResponse.success(true);
            }
        }

        try {
            String subject = req.getSubject() == null ? null : req.getSubject().trim();
            if (subject == null || subject.isBlank()) {
                String content = req.getContent() == null ? "" : req.getContent().trim();
                String firstLine = content.split("\\R", 2)[0].trim();
                subject = firstLine.isBlank() ? null : (firstLine.length() > 60 ? firstLine.substring(0, 60) : firstLine);
            }
            if (subject == null) {
                return ApiResponse.error("主题内容不能为空");
            }

            ThoughtEntity entity = new ThoughtEntity();
            entity.setSubject(subject);
            entity.setContent(req.getContent());
            entity.setUserId(userId);

            String themeKey = req.getThemeKey();
            if (themeKey != null && ALLOWED_THEME_KEYS.contains(themeKey)) {
                entity.setThemeKey(themeKey);
            }

            String normalizedStatus = normalizeStatus(req.getStatus());
            if (normalizedStatus != null) {
                entity.setStatus(normalizedStatus);
            } else {
                entity.setStatus("pending");
            }

            entity.setCreateUser(userId);
            entity.setUpdateTime(LocalDateTime.now());

            thoughtMapper.insert(entity);

            List<ThoughtSaveEventReq> events = req.getEvents();
            if (events != null) {
                for (ThoughtSaveEventReq eventReq : events) {
                    if (!StringUtils.hasText(eventReq.getContent())) {
                        continue;
                    }
                    ThoughtRelaEventEntity eventEntity = new ThoughtRelaEventEntity();
                    eventEntity.setThoughtId(entity.getId());
                    eventEntity.setContent(eventReq.getContent());
                    relaEventMapper.insert(eventEntity);
                }
            }

            return ApiResponse.success(true);
        } catch (Exception e) {
            if (idempotencyRedisKey != null) {
                redisUtil.delete(idempotencyRedisKey);
            }
            throw e;
        }
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
}

