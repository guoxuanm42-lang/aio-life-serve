package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.mapper.ITaskMapper;
import top.aiolife.record.pojo.entity.TaskEntity;
import top.aiolife.record.pojo.entity.TaskTypeEntity;
import top.aiolife.record.service.ITaskDetail;
import top.aiolife.record.service.ITaskService;
import top.aiolife.record.service.ITaskTypeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 代办任务控制器，提供清单任务查询、创建、更新、删除、排序与基础筛选接口。
 *
 * @author Ethan
 * @date 2026-05-30
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private static final int TODO_STATUS_PENDING = 0;

    private static final int TODO_STATUS_COMPLETED = 1;

    private static final int TODO_STATUS_FAILED = 2;

    private final ITaskService taskService;

    private final ITaskMapper taskMapper;

    private final ITaskDetail taskDetailService;

    private final ITaskTypeService taskTypeService;

    /**
     * 获取任务 Mapper。
     *
     * @return 任务 Mapper
     *
     * @author Ethan
     * @date 2026-05-30
     */
    public ITaskMapper getBaseMapper() {
        return taskMapper;
    }

    /**
     * 查询当前用户的代办任务列表。
     *
     * <p>用途：前端加载代办清单，支持按任务 id、主题、类型、完成状态和结束日期范围筛选。</p>
     *
     * @param taskId 任务 ID，可为空；传入时只查询该任务
     * @param get 当前页码
     * @param pageSize 每页数量
     * @param theme 类型所属主题，可为空
     * @param typeId 类型 ID，可为空
     * @param isCompleted 任务状态：0-未完成，1-已完成，2-已失败
     * @param statusGroup 状态分组，可为空；valid 表示有效任务（未完成与已完成）
     * @param startDate 结束时间筛选开始日期，可为空
     * @param endDate 结束时间筛选结束日期，可为空
     * @param hasFailureReason 失败原因是否已填写，可为空；仅用于失败代办筛选
     * @return 统一返回结构，data.items 为代办任务列表，data.total 为总数
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @GetMapping()
    public ApiResponse<PageResp<TaskEntity>> query(@RequestParam(required = false) Long taskId,
                                                   @RequestParam(defaultValue = "1") int get,
                                                   @RequestParam(defaultValue = "100") int pageSize,
                                                   @RequestParam(required = false) String theme,
                                                   @RequestParam(required = false) Long typeId,
                                                   @RequestParam(required = false) Integer isCompleted,
                                                   @RequestParam(required = false) String statusGroup,
                                                   @RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate,
                                                   @RequestParam(required = false) Boolean hasFailureReason) {
        Long userId = StpUtil.getLoginIdAsLong();
        markExpiredTasksFailed(userId);

        List<TaskTypeEntity> activeTaskTypes = taskTypeService.listUserTypes(userId);
        List<TaskTypeEntity> allTaskTypes = taskTypeService.listAllUserTypes(userId);
        List<Long> themeTypeIds = filterTypeIdsByTheme(activeTaskTypes, theme);
        if (StringUtils.hasText(theme) && themeTypeIds.isEmpty()) {
            return ApiResponse.success(PageResp.of(Collections.emptyList(), 0L));
        }

        LambdaQueryWrapper<TaskEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TaskEntity::getUserId, userId);
        if (taskId != null) {
            lambdaQueryWrapper.eq(TaskEntity::getId, taskId);
        }
        if (typeId != null) {
            lambdaQueryWrapper.eq(TaskEntity::getTypeId, typeId);
        }
        if (StringUtils.hasText(theme)) {
            lambdaQueryWrapper.in(TaskEntity::getTypeId, themeTypeIds);
        }
        if (isCompleted != null) {
            validateTodoStatus(isCompleted);
            lambdaQueryWrapper.eq(TaskEntity::getIsCompleted, isCompleted);
        } else if ("valid".equals(statusGroup)) {
            lambdaQueryWrapper.in(TaskEntity::getIsCompleted, TODO_STATUS_PENDING, TODO_STATUS_COMPLETED);
        }
        if (hasFailureReason != null) {
            if (isCompleted != null && !Objects.equals(isCompleted, TODO_STATUS_FAILED)) {
                return ApiResponse.success(PageResp.of(Collections.emptyList(), 0L));
            }
            lambdaQueryWrapper.eq(TaskEntity::getIsCompleted, TODO_STATUS_FAILED);
            if (hasFailureReason) {
                lambdaQueryWrapper.isNotNull(TaskEntity::getFailureReason);
                lambdaQueryWrapper.ne(TaskEntity::getFailureReason, "");
            } else {
                lambdaQueryWrapper.and(wrapper -> wrapper
                        .isNull(TaskEntity::getFailureReason)
                        .or()
                        .eq(TaskEntity::getFailureReason, ""));
            }
        }

        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);
        if (start != null) {
            lambdaQueryWrapper.ge(TaskEntity::getEndTime, start);
        }
        if (end != null) {
            lambdaQueryWrapper.le(TaskEntity::getEndTime, end);
        }

        lambdaQueryWrapper.orderByAsc(TaskEntity::getIsCompleted);
        lambdaQueryWrapper.orderByAsc(TaskEntity::getStartTime);
        lambdaQueryWrapper.orderByAsc(TaskEntity::getEndTime);
        lambdaQueryWrapper.orderByAsc(TaskEntity::getCreateTime);
        lambdaQueryWrapper.orderByAsc(TaskEntity::getSortOrder);
        Page<TaskEntity> page = new Page<>(get, pageSize);
        IPage<TaskEntity> iPage = getBaseMapper().selectPage(page, lambdaQueryWrapper);
        List<TaskEntity> records = iPage.getRecords();
        fillTaskTypeDisplayFields(records, allTaskTypes);
        fillUnCompletedDetailCount(records, userId);

        PageResp<TaskEntity> objectPageResp = PageResp.of(iPage.getRecords(), iPage.getTotal());
        return ApiResponse.success(objectPageResp);
    }

    /**
     * 创建代办任务接口。
     *
     * <p>用途：前端提交新代办内容，后端补充当前用户、默认时间、默认状态，并支持保存类型。</p>
     *
     * @param entity 代办任务请求体（包含内容、备注、栏目、类型、开始时间、结束时间等）
     * @return 统一返回结构，data 为创建后的代办任务
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @PostMapping("/save")
    public ApiResponse<TaskEntity> save(@RequestBody TaskEntity entity) {
        entity.setId(null);
        entity.setUserId(StpUtil.getLoginIdAsLong());
        entity.setIsDeleted(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        normalizeTypeId(entity);
        fillDefaultTodoFields(entity);
        normalizeFailureReason(entity);
        getBaseMapper().insert(entity);
        return ApiResponse.success(entity);
    }

    /**
     * 更新代办任务接口。
     *
     * <p>用途：前端保存标题、时间、备注、类型、完成状态或失败原因，后端按当前用户与任务 id 限定更新范围。</p>
     *
     * @param entity 代办任务请求体（必须包含 id）
     * @return 统一返回结构，data 表示是否更新成功
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @PostMapping("/update")
    public ApiResponse<Boolean> update(@RequestBody TaskEntity entity) {
        Long userId = StpUtil.getLoginIdAsLong();
        TaskEntity exist = selectOwnedTask(entity.getId(), userId);
        entity.setUserId(null);
        entity.setIsDeleted(null);
        entity.setCreateTime(null);
        entity.setUpdateTime(LocalDateTime.now());
        boolean shouldClearType = Objects.equals(entity.getTypeId(), 0L);
        normalizeTypeId(entity);
        normalizeFailureReason(entity, exist);
        LambdaQueryWrapper<TaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskEntity::getId, entity.getId());
        wrapper.eq(TaskEntity::getUserId, userId);
        getBaseMapper().update(entity, wrapper);
        clearFailureReasonIfNeeded(entity, exist, userId);
        clearTypeIfNeeded(entity.getId(), userId, shouldClearType);
        return ApiResponse.success();
    }

    /**
     * 删除代办任务接口。
     *
     * <p>用途：前端删除代办任务，后端按当前用户与任务 id 限定删除范围。</p>
     *
     * @param entity 代办任务请求体（只需要 id）
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody TaskEntity entity) {
        LambdaQueryWrapper<TaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskEntity::getId, entity.getId());
        wrapper.eq(TaskEntity::getUserId, StpUtil.getLoginIdAsLong());
        getBaseMapper().delete(wrapper);
        return ApiResponse.success();
    }

    /**
     * 重排代办任务接口。
     *
     * <p>用途：兼容旧看板拖拽排序，按当前用户限定任务更新范围。</p>
     *
     * @param list 代办任务排序请求列表（通常只传 id、columnId 和 sortOrder）
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @PostMapping("/reSort")
    public ApiResponse<Void> reSort(@RequestBody List<TaskEntity> list) {
        Long userId = StpUtil.getLoginIdAsLong();
        for (TaskEntity entity : list) {
            LambdaQueryWrapper<TaskEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskEntity::getId, entity.getId());
            wrapper.eq(TaskEntity::getUserId, userId);
            getBaseMapper().update(entity, wrapper);
        }
        return ApiResponse.success();
    }

    private void markExpiredTasksFailed(Long userId) {
        LambdaUpdateWrapper<TaskEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TaskEntity::getUserId, userId);
        wrapper.eq(TaskEntity::getIsCompleted, TODO_STATUS_PENDING);
        wrapper.lt(TaskEntity::getEndTime, LocalDateTime.now());
        wrapper.set(TaskEntity::getIsCompleted, TODO_STATUS_FAILED);
        wrapper.set(TaskEntity::getUpdateTime, LocalDateTime.now());
        getBaseMapper().update(null, wrapper);
    }

    private void fillDefaultTodoFields(TaskEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getStartTime() == null) {
            entity.setStartTime(now);
        }
        if (entity.getEndTime() == null) {
            entity.setEndTime(LocalDate.now().atTime(LocalTime.MAX.withNano(0)));
        }
        if (entity.getIsCompleted() == null) {
            entity.setIsCompleted(TODO_STATUS_PENDING);
        }
        validateTodoStatus(entity.getIsCompleted());
        if (entity.getDueDate() == null) {
            entity.setDueDate(entity.getEndTime());
        }
    }

    private void normalizeFailureReason(TaskEntity entity) {
        normalizeFailureReason(entity, null);
    }

    private void normalizeTypeId(TaskEntity entity) {
        if (Objects.equals(entity.getTypeId(), 0L)) {
            entity.setTypeId(null);
        }
    }

    private void normalizeFailureReason(TaskEntity entity, TaskEntity exist) {
        if (entity.getIsCompleted() != null) {
            validateTodoStatus(entity.getIsCompleted());
        }
        Integer nextStatus = entity.getIsCompleted() == null && exist != null
                ? exist.getIsCompleted()
                : entity.getIsCompleted();
        if (!Objects.equals(nextStatus, TODO_STATUS_FAILED)) {
            entity.setFailureReason(null);
        } else if (StringUtils.hasText(entity.getFailureReason())) {
            entity.setFailureReason(entity.getFailureReason().trim());
        }
    }

    private void clearFailureReasonIfNeeded(TaskEntity entity, TaskEntity exist, Long userId) {
        Integer nextStatus = entity.getIsCompleted() == null ? exist.getIsCompleted() : entity.getIsCompleted();
        if (Objects.equals(nextStatus, TODO_STATUS_FAILED)) {
            return;
        }
        if (entity.getIsCompleted() == null && entity.getFailureReason() == null) {
            return;
        }
        LambdaUpdateWrapper<TaskEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TaskEntity::getId, entity.getId());
        wrapper.eq(TaskEntity::getUserId, userId);
        wrapper.set(TaskEntity::getFailureReason, null);
        wrapper.set(TaskEntity::getUpdateTime, LocalDateTime.now());
        getBaseMapper().update(null, wrapper);
    }

    private void clearTypeIfNeeded(Long taskId, Long userId, boolean shouldClearType) {
        if (!shouldClearType) {
            return;
        }
        LambdaUpdateWrapper<TaskEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TaskEntity::getId, taskId);
        wrapper.eq(TaskEntity::getUserId, userId);
        wrapper.set(TaskEntity::getTypeId, null);
        wrapper.set(TaskEntity::getUpdateTime, LocalDateTime.now());
        getBaseMapper().update(null, wrapper);
    }

    private void validateTodoStatus(Integer status) {
        if (!Objects.equals(status, TODO_STATUS_PENDING)
                && !Objects.equals(status, TODO_STATUS_COMPLETED)
                && !Objects.equals(status, TODO_STATUS_FAILED)) {
            throw new IllegalArgumentException("isCompleted 只能是 0、1 或 2");
        }
    }

    private TaskEntity selectOwnedTask(Long taskId, Long userId) {
        if (taskId == null) {
            throw new IllegalArgumentException("任务 ID 不能为空");
        }
        LambdaQueryWrapper<TaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskEntity::getId, taskId);
        wrapper.eq(TaskEntity::getUserId, userId);
        TaskEntity exist = getBaseMapper().selectOne(wrapper);
        if (exist == null) {
            throw new IllegalArgumentException("代办不存在");
        }
        return exist;
    }

    private List<Long> filterTypeIdsByTheme(List<TaskTypeEntity> taskTypes, String theme) {
        if (!StringUtils.hasText(theme)) {
            return List.of();
        }
        return taskTypes.stream()
                .filter(type -> Objects.equals(theme.trim(), type.getTheme()))
                .map(TaskTypeEntity::getId)
                .toList();
    }

    private void fillTaskTypeDisplayFields(List<TaskEntity> records, List<TaskTypeEntity> taskTypes) {
        Map<Long, TaskTypeEntity> typeMap = taskTypes.stream()
                .collect(Collectors.toMap(TaskTypeEntity::getId, Function.identity(), (left, right) -> left));
        for (TaskEntity record : records) {
            if (record.getTypeId() == null) {
                continue;
            }
            TaskTypeEntity type = typeMap.get(record.getTypeId());
            if (type == null) {
                continue;
            }
            record.setTypeName(type.getName());
            record.setTheme(type.getTheme());
            record.setTypeColor(type.getColor());
            record.setTypeDeleted(!Objects.equals(type.getIsDeleted(), 0));
        }
    }

    private void fillUnCompletedDetailCount(List<TaskEntity> records, Long userId) {
        List<Long> taskIdList = records.stream().map(TaskEntity::getId).toList();
        Map<Long, Integer> unCompletedCountMap = taskDetailService.getUnCompletedCount(taskIdList, userId);
        records.forEach(record -> {
            Integer count = unCompletedCountMap.get(record.getId());
            record.setUnCompletedCount(count != null ? count : 0);
        });
    }

    private LocalDateTime parseStartDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        if (text.length() <= 10) {
            return LocalDate.parse(text).atStartOfDay();
        }
        return LocalDateTime.parse(text.replace(' ', 'T'));
    }

    private LocalDateTime parseEndDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        if (text.length() <= 10) {
            return LocalDate.parse(text).atTime(LocalTime.MAX.withNano(0));
        }
        return LocalDateTime.parse(text.replace(' ', 'T'));
    }
}
