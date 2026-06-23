package top.aiolife.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;

/**
 * 题目记录 Mapper，提供题目记录表的基础数据访问能力。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Mapper
public interface IProblemNoteMapper extends BaseMapper<ProblemNoteEntity> {
}
