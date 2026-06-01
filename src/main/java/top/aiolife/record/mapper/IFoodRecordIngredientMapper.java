package top.aiolife.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.record.pojo.entity.FoodRecordIngredientEntity;

/**
 * 美食记录材料 Mapper。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Mapper
public interface IFoodRecordIngredientMapper extends BaseMapper<FoodRecordIngredientEntity> {
}
