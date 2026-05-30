package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.record.pojo.entity.TaskTypeEntity;
import top.aiolife.record.service.ITaskTypeService;

import java.util.List;

/**
 * 代办类型控制器，提供当前用户的类型查询、创建、修改和删除接口。
 *
 * @author Ethan
 * @date 2026-05-30
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/taskTypes")
public class TaskTypeController {

    private final ITaskTypeService taskTypeService;

    /**
     * 查询当前用户的代办类型列表。
     *
     * <p>用途：新增代办、筛选代办和后续配置模块加载可选类型。</p>
     *
     * @return 统一返回结构，data 为当前用户未删除的代办类型列表
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @GetMapping
    public ApiResponse<List<TaskTypeEntity>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(taskTypeService.listUserTypes(userId));
    }

    /**
     * 新增代办类型接口。
     *
     * <p>用途：配置当前用户可选择的代办类型，主题和颜色跟随类型保存。</p>
     *
     * @param entity 类型保存请求体（包含类型名称、主题、颜色等）
     * @return 统一返回结构，data 为创建后的代办类型
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @PostMapping("/save")
    public ApiResponse<TaskTypeEntity> save(@RequestBody TaskTypeEntity entity) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(taskTypeService.create(entity, userId));
    }

    /**
     * 修改代办类型接口。
     *
     * <p>用途：更新当前用户已有代办类型的名称、主题、颜色或排序。</p>
     *
     * @param entity 类型更新请求体（必须包含 id）
     * @return 统一返回结构，data 为更新后的代办类型
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @PostMapping("/update")
    public ApiResponse<TaskTypeEntity> update(@RequestBody TaskTypeEntity entity) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(taskTypeService.update(entity, userId));
    }

    /**
     * 删除代办类型接口。
     *
     * <p>用途：逻辑删除当前用户的代办类型，不清理历史代办上的 typeId。</p>
     *
     * @param entity 删除请求体（只需要 id）
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody TaskTypeEntity entity) {
        Long userId = StpUtil.getLoginIdAsLong();
        taskTypeService.delete(entity.getId(), userId);
        return ApiResponse.success();
    }
}
