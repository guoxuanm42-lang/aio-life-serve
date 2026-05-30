package top.aiolife.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.aiolife.record.pojo.entity.TaskTypeEntity;

import java.util.List;

/**
 * 代办类型 Mapper，提供 task_type 表基础访问能力。
 *
 * @author Ethan
 * @date 2026-05-30
 */
@Mapper
public interface ITaskTypeMapper extends BaseMapper<TaskTypeEntity> {

    /**
     * 查询当前用户全部代办类型，包含已逻辑删除类型，用于历史代办展示。
     *
     * @param userId 当前用户 ID
     * @return 当前用户全部代办类型列表
     *
     * @author Ethan
     * @date 2026-05-30
     */
    @Select("""
            SELECT id, user_id, name, theme, color, sort_order, is_deleted, create_time, update_time
            FROM task_type
            WHERE user_id = #{userId}
            ORDER BY theme ASC, sort_order ASC, create_time ASC
            """)
    List<TaskTypeEntity> selectAllByUserId(Long userId);
}
