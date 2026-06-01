package top.aiolife.record.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.core.resq.PageResp;
import top.aiolife.mcp.pojo.req.FoodRecordQueryToolReq;
import top.aiolife.mcp.pojo.vo.FoodRecordQueryToolVO;
import top.aiolife.record.mapper.IFoodRecordMapper;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.req.FoodRecordSaveReq;
import top.aiolife.record.pojo.vo.FoodRecordDetailVO;
import top.aiolife.record.util.RedisUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 美食记录 MCP/AI 适配门面，负责文字工具保存、查询和幂等控制。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Component
@RequiredArgsConstructor
public class FoodRecordAiFacade {

    private static final long IDEMPOTENCY_TTL_SECONDS = TimeUnit.HOURS.toSeconds(24);

    private static final Set<String> ALLOWED_STATUSES = Set.of("draft", "done", "to_improve", "archived");

    private static final String LOCK_VALUE = "LOCK";

    private final IFoodRecordService foodRecordService;

    private final IFoodRecordMapper foodRecordMapper;

    private final RedisUtil redisUtil;

    /**
     * 保存美食记录，创建时支持幂等键，更新时必须指定记录 ID。
     *
     * @param req 美食记录保存请求
     * @param userId 当前用户 ID
     * @param idempotencyKey 幂等键，创建时可选
     * @return 美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    public FoodRecordDetailVO save(FoodRecordSaveReq req, long userId, String idempotencyKey) {
        if (req != null && req.getId() != null) {
            return foodRecordService.update(req, userId);
        }

        String idempotencyRedisKey = buildIdempotencyKey(userId, idempotencyKey);
        if (idempotencyRedisKey == null) {
            return foodRecordService.create(req, userId);
        }

        String existing = redisUtil.get(idempotencyRedisKey);
        if (StringUtils.hasText(existing) && !LOCK_VALUE.equals(existing)) {
            return foodRecordService.detail(Long.parseLong(existing), userId);
        }

        Boolean locked = redisUtil.setIfAbsent(idempotencyRedisKey, LOCK_VALUE, IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            String savedId = redisUtil.get(idempotencyRedisKey);
            if (StringUtils.hasText(savedId) && !LOCK_VALUE.equals(savedId)) {
                return foodRecordService.detail(Long.parseLong(savedId), userId);
            }
            return null;
        }

        try {
            FoodRecordDetailVO detail = foodRecordService.create(req, userId);
            redisUtil.set(idempotencyRedisKey, String.valueOf(detail.getRecord().getId()), IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
            return detail;
        } catch (Exception e) {
            redisUtil.delete(idempotencyRedisKey);
            throw e;
        }
    }

    /**
     * 查询当前用户的美食记录摘要，供 MCP 工具返回给 Agent。
     *
     * @param req MCP 查询请求
     * @param userId 当前用户 ID
     * @return 美食记录摘要分页
     *
     * @author Ethan
     * @date 2026-05-31
     */
    public PageResp<FoodRecordQueryToolVO> query(FoodRecordQueryToolReq req, long userId) {
        FoodRecordQueryToolReq safeReq = req == null ? new FoodRecordQueryToolReq() : req;
        LambdaQueryWrapper<FoodRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodRecordEntity::getUserId, userId);
        if (StringUtils.hasText(safeReq.getKeyword())) {
            wrapper.like(FoodRecordEntity::getDishName, safeReq.getKeyword().trim());
        }
        if (StringUtils.hasText(safeReq.getCategory())) {
            wrapper.eq(FoodRecordEntity::getCategory, safeReq.getCategory().trim());
        }
        if (StringUtils.hasText(safeReq.getMealType())) {
            wrapper.eq(FoodRecordEntity::getMealType, safeReq.getMealType().trim());
        }
        String status = resolveStatus(safeReq);
        if (status != null) {
            wrapper.eq(FoodRecordEntity::getStatus, status);
        }
        if (StringUtils.hasText(safeReq.getTags())) {
            wrapper.like(FoodRecordEntity::getTags, safeReq.getTags().trim());
        }
        if (safeReq.getStartDate() != null) {
            wrapper.ge(FoodRecordEntity::getCookDate, safeReq.getStartDate());
        }
        if (safeReq.getEndDate() != null) {
            wrapper.le(FoodRecordEntity::getCookDate, safeReq.getEndDate());
        }
        if (safeReq.getRating() != null) {
            wrapper.ge(FoodRecordEntity::getRating, safeReq.getRating());
        }
        if (safeReq.getWorthRedo() != null) {
            wrapper.eq(FoodRecordEntity::getWorthRedo, safeReq.getWorthRedo());
        }
        wrapper.orderByDesc(FoodRecordEntity::getCookDate);
        wrapper.orderByDesc(FoodRecordEntity::getUpdateTime);

        Page<FoodRecordEntity> page = new Page<>(normalizePage(safeReq.getPage()), normalizePageSize(safeReq.getPageSize()));
        IPage<FoodRecordEntity> result = foodRecordMapper.selectPage(page, wrapper);
        List<FoodRecordQueryToolVO> rows = result.getRecords().stream().map(this::toToolVO).toList();
        return PageResp.of(rows, result.getTotal());
    }

    private FoodRecordQueryToolVO toToolVO(FoodRecordEntity entity) {
        FoodRecordQueryToolVO vo = new FoodRecordQueryToolVO();
        vo.setId(entity.getId());
        vo.setDishName(entity.getDishName());
        vo.setCookDate(entity.getCookDate());
        vo.setCategory(entity.getCategory());
        vo.setMealType(entity.getMealType());
        vo.setTotalMinutes(entity.getTotalMinutes());
        vo.setRating(entity.getRating());
        vo.setSummary(entity.getSummary());
        vo.setNextImprove(entity.getNextImprove());
        vo.setProblems(entity.getProblems());
        vo.setTags(entity.getTags());
        vo.setStatus(entity.getStatus());
        vo.setWorthRedo(entity.getWorthRedo());
        return vo;
    }

    private String resolveStatus(FoodRecordQueryToolReq req) {
        if (Boolean.TRUE.equals(req.getToImprove())) {
            return "to_improve";
        }
        if (!StringUtils.hasText(req.getStatus())) {
            return null;
        }
        String status = req.getStatus().trim().toLowerCase();
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("美食记录状态不支持: " + req.getStatus());
        }
        return status;
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 200);
    }

    private String buildIdempotencyKey(long userId, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return null;
        }
        return "mcp:idemp:food_record_save:" + userId + ":" + idempotencyKey.trim();
    }
}
