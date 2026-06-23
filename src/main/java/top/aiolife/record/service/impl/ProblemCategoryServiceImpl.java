package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.IProblemCategoryMapper;
import top.aiolife.record.mapper.IProblemNoteMapper;
import top.aiolife.record.pojo.entity.ProblemCategoryEntity;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;
import top.aiolife.record.pojo.req.ProblemCategorySaveReq;
import top.aiolife.record.pojo.vo.ProblemCategoryListVO;
import top.aiolife.record.pojo.vo.ProblemCategoryVO;
import top.aiolife.record.service.IProblemCategoryService;

import java.util.List;

/**
 * 题目分类服务实现，按当前用户隔离分类并维护分类下题目归属。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Service
@RequiredArgsConstructor
public class ProblemCategoryServiceImpl extends ServiceImpl<IProblemCategoryMapper, ProblemCategoryEntity> implements IProblemCategoryService {

    private final IProblemCategoryMapper problemCategoryMapper;

    private final IProblemNoteMapper problemNoteMapper;

    /**
     * 查询当前用户的题目分类列表和题目数量。
     *
     * @param userId 当前用户 ID
     * @return 分类列表展示对象
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @Override
    public ProblemCategoryListVO listWithCount(Long userId) {
        List<ProblemCategoryEntity> categories = problemCategoryMapper.selectList(new LambdaQueryWrapper<ProblemCategoryEntity>()
                .eq(ProblemCategoryEntity::getUserId, userId)
                .orderByAsc(ProblemCategoryEntity::getSortOrder)
                .orderByAsc(ProblemCategoryEntity::getCreateTime));

        List<ProblemCategoryVO> categoryVOS = categories.stream()
                .map(item -> ProblemCategoryVO.of(item, countByCategory(userId, item.getId())))
                .toList();

        ProblemCategoryListVO vo = new ProblemCategoryListVO();
        vo.setTotalCount(problemNoteMapper.selectCount(new LambdaQueryWrapper<ProblemNoteEntity>()
                .eq(ProblemNoteEntity::getUserId, userId)));
        vo.setUncategorizedCount(problemNoteMapper.selectCount(new LambdaQueryWrapper<ProblemNoteEntity>()
                .eq(ProblemNoteEntity::getUserId, userId)
                .isNull(ProblemNoteEntity::getCategoryId)));
        vo.setCategories(categoryVOS);
        return vo;
    }

    /**
     * 新增题目分类。
     *
     * @param req 保存请求
     * @param userId 当前用户 ID
     * @return 新增后的题目分类
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProblemCategoryEntity create(ProblemCategorySaveReq req, Long userId) {
        ProblemCategoryEntity entity = new ProblemCategoryEntity();
        entity.setUserId(userId);
        entity.setName(normalizeRequiredName(req));
        entity.setSortOrder(resolveSortOrder(req));
        entity.fillCreateCommonField(userId);
        problemCategoryMapper.insert(entity);
        return entity;
    }

    /**
     * 更新题目分类。
     *
     * @param req 保存请求，必须包含 ID
     * @param userId 当前用户 ID
     * @return 更新后的题目分类
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProblemCategoryEntity update(ProblemCategorySaveReq req, Long userId) {
        if (req == null || req.getId() == null) {
            throw new IllegalArgumentException("题目分类 ID 不能为空");
        }
        ProblemCategoryEntity target = getOwnedCategory(req.getId(), userId);
        target.setName(normalizeRequiredName(req));
        target.setSortOrder(resolveSortOrder(req));
        target.fillUpdateCommonField(userId);
        problemCategoryMapper.updateById(target);
        return target;
    }

    /**
     * 删除题目分类，并将该分类下题目改为未分类。
     *
     * @param id 分类 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        ProblemCategoryEntity category = getOwnedCategory(id, userId);
        problemNoteMapper.update(null, new LambdaUpdateWrapper<ProblemNoteEntity>()
                .set(ProblemNoteEntity::getCategoryId, null)
                .eq(ProblemNoteEntity::getUserId, userId)
                .eq(ProblemNoteEntity::getCategoryId, id));
        category.setIsDeleted(StatusConst.IS_DELETE);
        category.fillUpdateCommonField(userId);
        problemCategoryMapper.updateById(category);
    }

    private Long countByCategory(Long userId, Long categoryId) {
        return problemNoteMapper.selectCount(new LambdaQueryWrapper<ProblemNoteEntity>()
                .eq(ProblemNoteEntity::getUserId, userId)
                .eq(ProblemNoteEntity::getCategoryId, categoryId));
    }

    private ProblemCategoryEntity getOwnedCategory(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("题目分类 ID 不能为空");
        }
        ProblemCategoryEntity entity = problemCategoryMapper.selectOne(new LambdaQueryWrapper<ProblemCategoryEntity>()
                .eq(ProblemCategoryEntity::getId, id)
                .eq(ProblemCategoryEntity::getUserId, userId));
        if (entity == null) {
            throw new IllegalArgumentException("题目分类不存在或无权访问");
        }
        return entity;
    }

    private String normalizeRequiredName(ProblemCategorySaveReq req) {
        if (req == null || !StringUtils.hasText(req.getName())) {
            throw new IllegalArgumentException("题目分类名称不能为空");
        }
        return req.getName().trim();
    }

    private Integer resolveSortOrder(ProblemCategorySaveReq req) {
        if (req == null || req.getSortOrder() == null) {
            return 0;
        }
        return req.getSortOrder();
    }
}
