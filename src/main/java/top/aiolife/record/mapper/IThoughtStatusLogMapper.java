package top.aiolife.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.record.pojo.entity.ThoughtStatusLogEntity;

/**
 * 闪念状态流转日志 Mapper。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Mapper
public interface IThoughtStatusLogMapper extends BaseMapper<ThoughtStatusLogEntity> {
}
