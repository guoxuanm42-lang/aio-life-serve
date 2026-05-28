package top.aiolife.record.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.record.convertor.TimeRecordConvertor;
import top.aiolife.record.pojo.entity.TimeRecordEntity;
import top.aiolife.record.pojo.req.TimeRecordDateRangeReq;
import top.aiolife.record.pojo.req.TimeRecordReq;
import top.aiolife.record.pojo.vo.TimeRecordDateRangeVO;
import top.aiolife.record.util.RedisUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 时迹 MCP/AI 适配 Facade
 *
 * @author Ethan
 */
@Component
public class TimeRecordAiFacade {

    private static final long IDEMPOTENCY_TTL_SECONDS = TimeUnit.HOURS.toSeconds(24);

    private final ITimeRecordService timeRecordService;
    private final ITimeTrackerCategoryService timeTrackerCategoryService;
    private final RedisUtil redisUtil;

    public TimeRecordAiFacade(ITimeRecordService timeRecordService,
                              ITimeTrackerCategoryService timeTrackerCategoryService,
                              RedisUtil redisUtil) {
        this.timeRecordService = timeRecordService;
        this.timeTrackerCategoryService = timeTrackerCategoryService;
        this.redisUtil = redisUtil;
    }

    public List<TimeRecordDateRangeVO> queryByDateRangeForAI(TimeRecordDateRangeReq req, long userId) {
        List<TimeRecordEntity> list = timeRecordService.list(buildDateRangeForAIQueryWrapper(userId, req));
        List<TimeRecordDateRangeVO> voList = TimeRecordConvertor.INSTANCE.toDateRangeVOList(list);

        Set<String> categoryIds = list.stream()
                .map(TimeRecordEntity::getCategoryId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, String> categoryNameMap = Collections.emptyMap();
        if (!categoryIds.isEmpty()) {
            List<Long> ids = categoryIds.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            categoryNameMap = timeTrackerCategoryService.listByIds(ids).stream()
                    .collect(Collectors.toMap(
                            cat -> String.valueOf(cat.getId()),
                            top.aiolife.record.pojo.entity.entity.TimeTrackerCategoryEntity::getName
                    ));
        }

        for (int i = 0; i < voList.size(); i++) {
            String categoryId = list.get(i).getCategoryId();
            if (StringUtils.hasText(categoryId) && categoryNameMap.containsKey(categoryId)) {
                voList.get(i).setCategoryName(categoryNameMap.get(categoryId));
            }
            if ("".equals(list.get(i).getTitle())) {
                voList.get(i).setTitle(null);
            }
        }

        voList.sort(Comparator
                .comparing(TimeRecordDateRangeVO::getDate, Comparator.reverseOrder())
                .thenComparing(TimeRecordDateRangeVO::getStartTime, Comparator.reverseOrder()));

        return voList;
    }

    public void saveTimeRecord(TimeRecordReq req, long userId, String idempotencyKey) {
        String idempotencyRedisKey = buildIdempotencyKey(userId, idempotencyKey);
        if (idempotencyRedisKey != null) {
            Boolean locked = redisUtil.setIfAbsent(idempotencyRedisKey, "1", IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(locked)) {
                return;
            }
        }

        try {
            if (req != null) {
                req.setId(null);
                req.setDuration(null);
            }
            timeRecordService.saveTimeRecord(req);
        } catch (Exception e) {
            if (idempotencyRedisKey != null) {
                redisUtil.delete(idempotencyRedisKey);
            }
            throw e;
        }
    }

    private LambdaQueryWrapper<TimeRecordEntity> buildDateRangeForAIQueryWrapper(long userId, TimeRecordDateRangeReq req) {
        LambdaQueryWrapper<TimeRecordEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(TimeRecordEntity::getId,
                TimeRecordEntity::getCategoryId,
                TimeRecordEntity::getDate,
                TimeRecordEntity::getStartTime,
                TimeRecordEntity::getEndTime,
                TimeRecordEntity::getTitle);
        lambdaQueryWrapper.eq(TimeRecordEntity::getUserId, userId);
        lambdaQueryWrapper.between(TimeRecordEntity::getDate, req.getStartDate(), req.getEndDate());
        lambdaQueryWrapper.orderByDesc(TimeRecordEntity::getUpdateTime);
        return lambdaQueryWrapper;
    }

    private String buildIdempotencyKey(long userId, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return null;
        }
        return "mcp:idemp:time_record_save:" + userId + ":" + idempotencyKey.trim();
    }
}

