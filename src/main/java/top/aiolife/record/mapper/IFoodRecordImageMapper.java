package top.aiolife.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.record.pojo.entity.FoodRecordImageEntity;

/**
 * 美食记录图片 Mapper，负责美食记录图片元数据的持久化。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Mapper
public interface IFoodRecordImageMapper extends BaseMapper<FoodRecordImageEntity> {
}
