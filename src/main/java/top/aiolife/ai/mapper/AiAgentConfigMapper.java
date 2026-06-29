package top.aiolife.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.ai.pojo.entity.AiAgentConfigEntity;

/**
 * AI Agent 配置数据访问 Mapper。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Mapper
public interface AiAgentConfigMapper extends BaseMapper<AiAgentConfigEntity> {
}
