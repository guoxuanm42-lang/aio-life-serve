package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 时迹分类耗时统计项，记录分类时长及其在总时长中的占比。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class CategoryDuration {

    private String categoryId;

    private String categoryName;

    private long durationMinutes;

    private BigDecimal percentage;
}
