package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.ITaskTypeMapper;
import top.aiolife.record.pojo.entity.TaskTypeEntity;
import top.aiolife.record.service.ITaskTypeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 代办类型服务实现，按当前用户隔离类型配置并校验类型名称唯一性。
 *
 * @author Ethan
 * @date 2026-05-30
 */
@Service
@RequiredArgsConstructor
public class TaskTypeServiceImpl extends ServiceImpl<ITaskTypeMapper, TaskTypeEntity> implements ITaskTypeService {

    private final ITaskTypeMapper taskTypeMapper;

    @Override
    public List<TaskTypeEntity> listUserTypes(Long userId) {
        LambdaQueryWrapper<TaskTypeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTypeEntity::getUserId, userId);
        wrapper.eq(TaskTypeEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.orderByAsc(TaskTypeEntity::getTheme);
        wrapper.orderByAsc(TaskTypeEntity::getSortOrder);
        wrapper.orderByAsc(TaskTypeEntity::getCreateTime);
        return taskTypeMapper.selectList(wrapper);
    }

    /**
     * 查询当前用户全部代办类型，包含已逻辑删除类型。
     *
     * @param userId 当前用户 ID
     * @return 当前用户全部代办类型列表
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @Override
    public List<TaskTypeEntity> listAllUserTypes(Long userId) {
        return taskTypeMapper.selectAllByUserId(userId);
    }

    @Override
    public TaskTypeEntity create(TaskTypeEntity entity, Long userId) {
        validateType(entity, userId, null);
        entity.setId(null);
        entity.setUserId(userId);
        entity.setName(entity.getName().trim());
        entity.setTheme(normalizeBlank(entity.getTheme()));
        entity.setColor(normalizeBlank(entity.getColor()));
        entity.setSortOrder(nextSortOrder(userId));
        entity.setIsDeleted(StatusConst.NO_DELETE);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        taskTypeMapper.insert(entity);
        return entity;
    }

    @Override
    public TaskTypeEntity update(TaskTypeEntity entity, Long userId) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("类型 ID 不能为空");
        }
        TaskTypeEntity exist = getOwnedType(entity.getId(), userId);
        validateType(entity, userId, entity.getId());
        exist.setName(entity.getName().trim());
        exist.setTheme(normalizeBlank(entity.getTheme()));
        exist.setColor(normalizeBlank(entity.getColor()));
        if (entity.getSortOrder() != null) {
            exist.setSortOrder(entity.getSortOrder());
        }
        exist.setUpdateTime(LocalDateTime.now());
        taskTypeMapper.updateById(exist);
        return exist;
    }

    @Override
    public void delete(Long id, Long userId) {
        TaskTypeEntity exist = getOwnedType(id, userId);
        exist.setIsDeleted(StatusConst.IS_DELETE);
        exist.setUpdateTime(LocalDateTime.now());
        taskTypeMapper.updateById(exist);
    }

    private TaskTypeEntity getOwnedType(Long id, Long userId) {
        LambdaQueryWrapper<TaskTypeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTypeEntity::getId, id);
        wrapper.eq(TaskTypeEntity::getUserId, userId);
        wrapper.eq(TaskTypeEntity::getIsDeleted, StatusConst.NO_DELETE);
        TaskTypeEntity exist = taskTypeMapper.selectOne(wrapper);
        if (exist == null) {
            throw new IllegalArgumentException("代办类型不存在");
        }
        return exist;
    }

    private void validateType(TaskTypeEntity entity, Long userId, Long ignoreId) {
        if (entity == null || !StringUtils.hasText(entity.getName())) {
            throw new IllegalArgumentException("类型名称不能为空");
        }
        LambdaQueryWrapper<TaskTypeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTypeEntity::getUserId, userId);
        wrapper.eq(TaskTypeEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.eq(TaskTypeEntity::getName, entity.getName().trim());
        if (ignoreId != null) {
            wrapper.ne(TaskTypeEntity::getId, ignoreId);
        }
        if (taskTypeMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("类型名称已存在");
        }
    }

    private Integer nextSortOrder(Long userId) {
        LambdaQueryWrapper<TaskTypeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTypeEntity::getUserId, userId);
        wrapper.eq(TaskTypeEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.orderByDesc(TaskTypeEntity::getSortOrder);
        wrapper.last("limit 1");
        TaskTypeEntity last = taskTypeMapper.selectOne(wrapper);
        return last == null || Objects.isNull(last.getSortOrder()) ? 1 : last.getSortOrder() + 1;
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
