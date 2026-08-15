package top.aiolife.ai.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;

/**
 * AI 活动总结生成任务 Mapper。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Mapper
public interface AiActivitySummaryGenerationMapper extends BaseMapper<AiActivitySummaryGenerationEntity> {
}
