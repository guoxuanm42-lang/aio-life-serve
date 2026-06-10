package top.aiolife.record.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 闪念统计趋势视图。
 *
 * <p>用途：承载新增趋势、分类趋势、活跃度和爆发日统计数据。</p>
 *
 * @author Ethan
 * @date 2026-06-10
 */
@Data
public class ThoughtStatisticsTrendVO {

    private String range;

    private String groupBy;

    private List<Point> trend;

    private List<CategoryTrend> categoryTrends;

    private List<Point> activity;

    private List<BurstDay> burstDays;

    /**
     * 趋势数据点。
     *
     * @author Ethan
     * @date 2026-06-10
     */
    @Data
    public static class Point {

        private String date;

        private long count;
    }

    /**
     * 分类趋势数据。
     *
     * @author Ethan
     * @date 2026-06-10
     */
    @Data
    public static class CategoryTrend {

        private String categoryKey;

        private String categoryName;

        private List<Point> points;
    }

    /**
     * 闪念爆发日数据。
     *
     * @author Ethan
     * @date 2026-06-10
     */
    @Data
    public static class BurstDay {

        private String date;

        private long count;

        private BigDecimal average;
    }
}
