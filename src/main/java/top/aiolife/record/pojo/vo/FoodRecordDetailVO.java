package top.aiolife.record.pojo.vo;

import lombok.Data;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.entity.FoodRecordImageEntity;
import top.aiolife.record.pojo.entity.FoodRecordIngredientEntity;
import top.aiolife.record.pojo.entity.FoodRecordStepEntity;

import java.util.List;

/**
 * 美食记录详情视图对象，包含主记录、材料清单和步骤流程。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordDetailVO {

    /**
     * 美食主记录。
     */
    private FoodRecordEntity record;

    /**
     * 材料清单。
     */
    private List<FoodRecordIngredientEntity> ingredients;

    /**
     * 步骤流程。
     */
    private List<FoodRecordStepEntity> steps;

    /**
     * 美食记录图片列表。
     */
    private List<FoodRecordImageEntity> images;
}
