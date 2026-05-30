package top.aiolife.record.service;

import top.aiolife.record.pojo.entity.TaskTypeEntity;

import java.util.List;

/**
 * 代办类型服务，负责当前用户的类型查询、创建、修改与删除。
 *
 * @author Ethan
 * @date 2026-05-30
 */
public interface ITaskTypeService {

    /**
     * 查询当前用户未删除的代办类型。
     *
     * @param userId 当前用户 ID
     * @return 代办类型列表
     *
     * @author Ethan
     * @date 2026-05-30
     */
    List<TaskTypeEntity> listUserTypes(Long userId);

    /**
     * 查询当前用户全部代办类型，包含已逻辑删除类型。
     *
     * @param userId 当前用户 ID
     * @return 当前用户全部代办类型列表
     *
     * @author Ethan
     * @date 2026-05-30
     */
    List<TaskTypeEntity> listAllUserTypes(Long userId);

    /**
     * 创建当前用户的代办类型。
     *
     * @param entity 类型保存对象
     * @param userId 当前用户 ID
     * @return 创建后的代办类型
     *
     * @author Ethan
     * @date 2026-05-30
     */
    TaskTypeEntity create(TaskTypeEntity entity, Long userId);

    /**
     * 更新当前用户的代办类型。
     *
     * @param entity 类型更新对象
     * @param userId 当前用户 ID
     * @return 更新后的代办类型
     *
     * @author Ethan
     * @date 2026-05-30
     */
    TaskTypeEntity update(TaskTypeEntity entity, Long userId);

    /**
     * 删除当前用户的代办类型。
     *
     * @param id 类型 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-05-30
     */
    void delete(Long id, Long userId);
}
