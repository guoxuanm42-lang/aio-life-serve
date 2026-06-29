package top.aiolife.ai.memory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.ai.memory.pojo.entity.AiMemoryEntity;

/**
 * AI 长期记忆数据访问 Mapper。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Mapper
public interface AiMemoryMapper extends BaseMapper<AiMemoryEntity> {
}
