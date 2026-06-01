package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 美食记录主实体，保存做饭基础信息、时间信息、评价和复盘内容。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("food_record")
public class FoodRecordEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

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
     * 材料数量，列表展示时可选填充。
     */
    @TableField(exist = false)
    private Integer ingredientCount;

    /**
     * 步骤数量，列表展示时可选填充。
     */
    @TableField(exist = false)
    private Integer stepCount;
}
