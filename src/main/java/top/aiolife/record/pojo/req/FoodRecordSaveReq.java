package top.aiolife.record.pojo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 美食记录保存请求，包含主记录、材料清单和步骤流程。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordSaveReq {

    /**
     * 美食记录 ID，更新时必填。
     */
    private Long id;

    /**
     * 菜名。
     */
    private String dishName;

    /**
     * 分类。
     */
    private String category;

    /**
     * 餐次。
     */
    private String mealType;

    /**
     * 做饭日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate cookDate;

    /**
     * 状态：draft/done/to_improve/archived。
     */
    private String status;

    /**
     * 标签，第一阶段以字符串保存。
     */
    private String tags;

    /**
     * 难度。
     */
    private String difficulty;

    /**
     * 评分。
     */
    private BigDecimal rating;

    /**
     * 成功程度。
     */
    private String successLevel;

    /**
     * 备菜时间（分钟）。
     */
    private Integer prepMinutes;

    /**
     * 烹饪时间（分钟）。
     */
    private Integer cookMinutes;

    /**
     * 总耗时（分钟）。
     */
    private Integer totalMinutes;

    /**
     * 口味描述。
     */
    private String tasteDescription;

    /**
     * 问题或不足。
     */
    private String problems;

    /**
     * 本次总结。
     */
    private String summary;

    /**
     * 一句话总结。
     */
    private String briefSummary;

    /**
     * 下次改进。
     */
    private String nextImprove;

    /**
     * 是否值得复做。
     */
    private Boolean worthRedo;

    /**
     * 下次尝试建议。
     */
    private String nextTrySuggestion;

    /**
     * 材料清单。
     */
    private List<FoodRecordIngredientSaveReq> ingredients;

    /**
     * 步骤流程。
     */
    private List<FoodRecordStepSaveReq> steps;
}
