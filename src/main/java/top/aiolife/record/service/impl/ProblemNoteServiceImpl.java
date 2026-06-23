package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.mapper.IProblemCategoryMapper;
import top.aiolife.record.mapper.IProblemNoteMapper;
import top.aiolife.record.pojo.entity.ProblemCategoryEntity;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;
import top.aiolife.record.pojo.req.ProblemNoteQueryReq;
import top.aiolife.record.pojo.req.ProblemNoteSaveReq;
import top.aiolife.record.service.IProblemNoteService;

import java.util.Set;

/**
 * 题目记录服务实现，按当前用户隔离题目记录并维护 Java 解法代码和思路备注。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Service
@RequiredArgsConstructor
public class ProblemNoteServiceImpl extends ServiceImpl<IProblemNoteMapper, ProblemNoteEntity> implements IProblemNoteService {

    private static final String DEFAULT_STATUS = "draft";

    private static final Set<String> ALLOWED_STATUSES = Set.of("draft", "solved", "reviewing", "archived");

    private final IProblemNoteMapper problemNoteMapper;

    private final IProblemCategoryMapper problemCategoryMapper;

    /**
     * 分页查询当前用户的题目记录。
     *
     * @param req 查询请求
     * @param userId 当前用户 ID
     * @return 题目记录分页数据
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @Override
    public PageResp<ProblemNoteEntity> query(ProblemNoteQueryReq req, Long userId) {
        ProblemNoteQueryReq safeReq = req == null ? new ProblemNoteQueryReq() : req;
        LambdaQueryWrapper<ProblemNoteEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProblemNoteEntity::getUserId, userId);
        if (StringUtils.hasText(safeReq.getKeyword())) {
            String keyword = safeReq.getKeyword().trim();
            wrapper.and(item -> item
                    .like(ProblemNoteEntity::getTitle, keyword)
                    .or()
                    .like(ProblemNoteEntity::getProblemContent, keyword)
                    .or()
                    .like(ProblemNoteEntity::getIdeaNote, keyword));
        }
        if (StringUtils.hasText(safeReq.getDifficulty())) {
            wrapper.eq(ProblemNoteEntity::getDifficulty, safeReq.getDifficulty().trim());
        }
        String status = normalizeStatus(safeReq.getStatus(), false);
        if (status != null) {
            wrapper.eq(ProblemNoteEntity::getStatus, status);
        }
        if (StringUtils.hasText(safeReq.getTags())) {
            wrapper.like(ProblemNoteEntity::getTags, safeReq.getTags().trim());
        }
        if (Boolean.TRUE.equals(safeReq.getUncategorized())) {
            wrapper.isNull(ProblemNoteEntity::getCategoryId);
        } else if (safeReq.getCategoryId() != null) {
            wrapper.eq(ProblemNoteEntity::getCategoryId, safeReq.getCategoryId());
        }
        wrapper.orderByDesc(ProblemNoteEntity::getUpdateTime);

        Page<ProblemNoteEntity> page = new Page<>(normalizePage(safeReq.getPage()), normalizePageSize(safeReq.getPageSize()));
        IPage<ProblemNoteEntity> iPage = problemNoteMapper.selectPage(page, wrapper);
        return PageResp.of(iPage.getRecords(), iPage.getTotal());
    }

    /**
     * 查询当前用户的题目记录详情。
     *
     * @param id 题目记录 ID
     * @param userId 当前用户 ID
     * @return 题目记录详情
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @Override
    public ProblemNoteEntity detail(Long id, Long userId) {
        return getOwnedRecord(id, userId);
    }

    /**
     * 新增题目记录。
     *
     * @param req 保存请求
     * @param userId 当前用户 ID
     * @return 新增后的题目记录
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProblemNoteEntity create(ProblemNoteSaveReq req, Long userId) {
        ProblemNoteEntity entity = buildEntity(req, userId, false);
        entity.fillCreateCommonField(userId);
        problemNoteMapper.insert(entity);
        return detail(entity.getId(), userId);
    }

    /**
     * 更新题目记录。
     *
     * @param req 保存请求，必须包含 ID
     * @param userId 当前用户 ID
     * @return 更新后的题目记录
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProblemNoteEntity update(ProblemNoteSaveReq req, Long userId) {
        if (req == null || req.getId() == null) {
            throw new IllegalArgumentException("题目记录 ID 不能为空");
        }
        getOwnedRecord(req.getId(), userId);
        ProblemNoteEntity entity = buildEntity(req, userId, true);
        entity.setId(req.getId());
        entity.fillUpdateCommonField(userId);
        problemNoteMapper.updateById(entity);
        return detail(req.getId(), userId);
    }

    /**
     * 逻辑删除题目记录。
     *
     * @param id 题目记录 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        ProblemNoteEntity entity = getOwnedRecord(id, userId);
        entity.setIsDeleted(StatusConst.IS_DELETE);
        entity.fillUpdateCommonField(userId);
        problemNoteMapper.updateById(entity);
    }

    private ProblemNoteEntity buildEntity(ProblemNoteSaveReq req, Long userId, boolean update) {
        if (req == null) {
            throw new IllegalArgumentException("题目记录不能为空");
        }
        ProblemNoteEntity entity = new ProblemNoteEntity();
        entity.setUserId(update ? null : userId);
        entity.setCategoryId(resolveCategoryId(req.getCategoryId(), userId));
        entity.setTitle(normalizeRequired(req.getTitle(), "题目标题不能为空"));
        entity.setProblemContent(normalizeRequired(req.getProblemContent(), "题目内容不能为空"));
        entity.setSolutionCode(normalizeBlank(req.getSolutionCode()));
        entity.setIdeaNote(normalizeBlank(req.getIdeaNote()));
        entity.setDifficulty(normalizeBlank(req.getDifficulty()));
        entity.setTags(normalizeBlank(req.getTags()));
        entity.setStatus(normalizeStatus(req.getStatus(), true));
        return entity;
    }

    private Long resolveCategoryId(Long categoryId, Long userId) {
        if (categoryId == null) {
            return null;
        }
        Long count = problemCategoryMapper.selectCount(new LambdaQueryWrapper<ProblemCategoryEntity>()
                .eq(ProblemCategoryEntity::getId, categoryId)
                .eq(ProblemCategoryEntity::getUserId, userId));
        if (count == null || count == 0) {
            throw new IllegalArgumentException("题目分类不存在或无权访问");
        }
        return categoryId;
    }

    private ProblemNoteEntity getOwnedRecord(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("题目记录 ID 不能为空");
        }
        ProblemNoteEntity entity = problemNoteMapper.selectOne(new LambdaQueryWrapper<ProblemNoteEntity>()
                .eq(ProblemNoteEntity::getId, id)
                .eq(ProblemNoteEntity::getUserId, userId));
        if (entity == null) {
            throw new IllegalArgumentException("题目记录不存在或无权访问");
        }
        return entity;
    }

    private String normalizeStatus(String status, boolean withDefault) {
        if (!StringUtils.hasText(status)) {
            return withDefault ? DEFAULT_STATUS : null;
        }
        String normalized = status.trim();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("题目状态不合法");
        }
        return normalized;
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

    private long normalizePage(Integer page) {
        return page == null || page < 1 ? 1L : page.longValue();
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 50L;
        }
        return Math.min(pageSize, 200);
    }
}
