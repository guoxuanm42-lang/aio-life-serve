package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.mapper.IFoodRecordIngredientMapper;
import top.aiolife.record.mapper.IFoodRecordMapper;
import top.aiolife.record.mapper.IFoodRecordStepMapper;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.entity.FoodRecordIngredientEntity;
import top.aiolife.record.pojo.entity.FoodRecordStepEntity;
import top.aiolife.record.pojo.req.FoodRecordIngredientSaveReq;
import top.aiolife.record.pojo.req.FoodRecordQueryReq;
import top.aiolife.record.pojo.req.FoodRecordSaveReq;
import top.aiolife.record.pojo.req.FoodRecordStepSaveReq;
import top.aiolife.record.pojo.vo.FoodRecordDetailVO;
import top.aiolife.record.service.IFoodRecordImageService;
import top.aiolife.record.service.IFoodRecordService;

import java.util.List;
import java.util.Set;

/**
 * 美食记录服务实现，按当前用户隔离美食记录并维护材料和步骤子项。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Service
@RequiredArgsConstructor
public class FoodRecordServiceImpl extends ServiceImpl<IFoodRecordMapper, FoodRecordEntity> implements IFoodRecordService {

    private static final String DEFAULT_STATUS = "draft";

    private static final Set<String> ALLOWED_STATUSES = Set.of("draft", "done", "to_improve", "archived");

    private final IFoodRecordMapper foodRecordMapper;

    private final IFoodRecordIngredientMapper ingredientMapper;

    private final IFoodRecordStepMapper stepMapper;

    private final IFoodRecordImageService imageService;

    /**
     * 分页查询当前用户的美食记录。
     *
     * @param req 查询请求
     * @param userId 当前用户 ID
     * @return 分页美食记录
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Override
    public PageResp<FoodRecordEntity> query(FoodRecordQueryReq req, Long userId) {
        FoodRecordQueryReq safeReq = req == null ? new FoodRecordQueryReq() : req;
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
        String status = normalizeStatus(safeReq.getStatus(), false);
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
        wrapper.orderByDesc(FoodRecordEntity::getCookDate);
        wrapper.orderByDesc(FoodRecordEntity::getUpdateTime);

        Page<FoodRecordEntity> page = new Page<>(normalizePage(safeReq.getPage()), normalizePageSize(safeReq.getPageSize()));
        IPage<FoodRecordEntity> iPage = foodRecordMapper.selectPage(page, wrapper);
        return PageResp.of(iPage.getRecords(), iPage.getTotal());
    }

    /**
     * 查询当前用户的美食记录详情。
     *
     * @param id 美食记录 ID
     * @param userId 当前用户 ID
     * @return 美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Override
    public FoodRecordDetailVO detail(Long id, Long userId) {
        FoodRecordEntity record = getOwnedRecord(id, userId);
        FoodRecordDetailVO vo = new FoodRecordDetailVO();
        vo.setRecord(record);
        vo.setIngredients(listIngredients(id, userId));
        vo.setSteps(listSteps(id, userId));
        vo.setImages(imageService.listByRecord(id, userId));
        return vo;
    }

    /**
     * 新增美食记录。
     *
     * @param req 保存请求
     * @param userId 当前用户 ID
     * @return 美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodRecordDetailVO create(FoodRecordSaveReq req, Long userId) {
        FoodRecordEntity entity = buildRecordEntity(req, userId, false);
        entity.fillCreateCommonField(userId);
        foodRecordMapper.insert(entity);
        replaceChildren(entity.getId(), userId, req);
        return detail(entity.getId(), userId);
    }

    /**
     * 更新美食记录。
     *
     * @param req 保存请求
     * @param userId 当前用户 ID
     * @return 美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodRecordDetailVO update(FoodRecordSaveReq req, Long userId) {
        if (req == null || req.getId() == null) {
            throw new IllegalArgumentException("美食记录 ID 不能为空");
        }
        getOwnedRecord(req.getId(), userId);
        FoodRecordEntity entity = buildRecordEntity(req, userId, true);
        entity.setId(req.getId());
        entity.fillUpdateCommonField(userId);
        foodRecordMapper.updateById(entity);
        replaceChildren(req.getId(), userId, req);
        return detail(req.getId(), userId);
    }

    /**
     * 逻辑删除美食记录及其材料和步骤。
     *
     * @param id 美食记录 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        FoodRecordEntity record = getOwnedRecord(id, userId);
        record.setIsDeleted(StatusConst.IS_DELETE);
        record.fillUpdateCommonField(userId);
        foodRecordMapper.updateById(record);
        logicDeleteIngredients(id, userId);
        logicDeleteSteps(id, userId);
    }

    private FoodRecordEntity buildRecordEntity(FoodRecordSaveReq req, Long userId, boolean update) {
        if (req == null) {
            throw new IllegalArgumentException("美食记录不能为空");
        }
        String dishName = normalizeRequired(req.getDishName(), "菜名不能为空");
        FoodRecordEntity entity = new FoodRecordEntity();
        entity.setUserId(update ? null : userId);
        entity.setDishName(dishName);
        entity.setCategory(normalizeBlank(req.getCategory()));
        entity.setMealType(normalizeBlank(req.getMealType()));
        entity.setCookDate(req.getCookDate());
        entity.setStatus(normalizeStatus(req.getStatus(), true));
        entity.setTags(normalizeBlank(req.getTags()));
        entity.setDifficulty(normalizeBlank(req.getDifficulty()));
        entity.setRating(req.getRating());
        entity.setSuccessLevel(normalizeBlank(req.getSuccessLevel()));
        entity.setPrepMinutes(req.getPrepMinutes());
        entity.setCookMinutes(req.getCookMinutes());
        entity.setTotalMinutes(resolveTotalMinutes(req));
        entity.setTasteDescription(normalizeBlank(req.getTasteDescription()));
        entity.setProblems(normalizeBlank(req.getProblems()));
        entity.setSummary(normalizeBlank(req.getSummary()));
        entity.setBriefSummary(normalizeBlank(req.getBriefSummary()));
        entity.setNextImprove(normalizeBlank(req.getNextImprove()));
        entity.setWorthRedo(req.getWorthRedo());
        entity.setNextTrySuggestion(normalizeBlank(req.getNextTrySuggestion()));
        return entity;
    }

    private void replaceChildren(Long recordId, Long userId, FoodRecordSaveReq req) {
        logicDeleteIngredients(recordId, userId);
        logicDeleteSteps(recordId, userId);
        insertIngredients(recordId, userId, req.getIngredients());
        insertSteps(recordId, userId, req.getSteps());
    }

    private void insertIngredients(Long recordId, Long userId, List<FoodRecordIngredientSaveReq> ingredients) {
        if (ingredients == null) {
            return;
        }
        int index = 0;
        for (FoodRecordIngredientSaveReq req : ingredients) {
            if (req == null || !StringUtils.hasText(req.getName())) {
                continue;
            }
            FoodRecordIngredientEntity entity = new FoodRecordIngredientEntity();
            entity.setUserId(userId);
            entity.setRecordId(recordId);
            entity.setName(req.getName().trim());
            entity.setQuantity(normalizeBlank(req.getQuantity()));
            entity.setUnit(normalizeBlank(req.getUnit()));
            entity.setRemark(normalizeBlank(req.getRemark()));
            entity.setSortOrder(req.getSortOrder() == null ? index : req.getSortOrder());
            entity.fillCreateCommonField(userId);
            ingredientMapper.insert(entity);
            index++;
        }
    }

    private void insertSteps(Long recordId, Long userId, List<FoodRecordStepSaveReq> steps) {
        if (steps == null) {
            return;
        }
        int index = 0;
        for (FoodRecordStepSaveReq req : steps) {
            if (req == null || (!StringUtils.hasText(req.getTitle()) && !StringUtils.hasText(req.getDescription()))) {
                continue;
            }
            FoodRecordStepEntity entity = new FoodRecordStepEntity();
            entity.setUserId(userId);
            entity.setRecordId(recordId);
            entity.setStepNo(req.getStepNo() == null ? index + 1 : req.getStepNo());
            entity.setTitle(normalizeBlank(req.getTitle()));
            entity.setDescription(normalizeBlank(req.getDescription()));
            entity.setDurationMinutes(req.getDurationMinutes());
            entity.setSortOrder(req.getSortOrder() == null ? index : req.getSortOrder());
            entity.fillCreateCommonField(userId);
            stepMapper.insert(entity);
            index++;
        }
    }

    private FoodRecordEntity getOwnedRecord(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("美食记录 ID 不能为空");
        }
        LambdaQueryWrapper<FoodRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodRecordEntity::getId, id);
        wrapper.eq(FoodRecordEntity::getUserId, userId);
        FoodRecordEntity record = foodRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new IllegalArgumentException("美食记录不存在或无权操作");
        }
        return record;
    }

    private List<FoodRecordIngredientEntity> listIngredients(Long recordId, Long userId) {
        QueryWrapper<FoodRecordIngredientEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId);
        wrapper.eq("user_id", userId);
        wrapper.orderByAsc("sort_order");
        wrapper.orderByAsc("create_time");
        return ingredientMapper.selectList(wrapper);
    }

    private List<FoodRecordStepEntity> listSteps(Long recordId, Long userId) {
        QueryWrapper<FoodRecordStepEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId);
        wrapper.eq("user_id", userId);
        wrapper.orderByAsc("sort_order");
        wrapper.orderByAsc("step_no");
        return stepMapper.selectList(wrapper);
    }

    private void logicDeleteIngredients(Long recordId, Long userId) {
        UpdateWrapper<FoodRecordIngredientEntity> wrapper = new UpdateWrapper<>();
        wrapper.set("is_deleted", StatusConst.IS_DELETE);
        wrapper.eq("record_id", recordId);
        wrapper.eq("user_id", userId);
        ingredientMapper.update(null, wrapper);
    }

    private void logicDeleteSteps(Long recordId, Long userId) {
        UpdateWrapper<FoodRecordStepEntity> wrapper = new UpdateWrapper<>();
        wrapper.set("is_deleted", StatusConst.IS_DELETE);
        wrapper.eq("record_id", recordId);
        wrapper.eq("user_id", userId);
        stepMapper.update(null, wrapper);
    }

    private String normalizeStatus(String status, boolean useDefault) {
        if (!StringUtils.hasText(status)) {
            return useDefault ? DEFAULT_STATUS : null;
        }
        String normalized = status.trim().toLowerCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("美食记录状态不支持: " + status);
        }
        return normalized;
    }

    private Integer resolveTotalMinutes(FoodRecordSaveReq req) {
        if (req.getTotalMinutes() != null) {
            return req.getTotalMinutes();
        }
        Integer prep = req.getPrepMinutes();
        Integer cook = req.getCookMinutes();
        if (prep == null && cook == null) {
            return null;
        }
        return (prep == null ? 0 : prep) + (cook == null ? 0 : cook);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 50;
        }
        return Math.min(pageSize, 200);
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
