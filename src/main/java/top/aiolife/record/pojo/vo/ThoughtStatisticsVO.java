package top.aiolife.record.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 闪念统计洞察视图。
 *
 * <p>用途：承载当前用户闪念总览指标、状态分布和分类分布数据。</p>
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Data
public class ThoughtStatisticsVO {

    private Summary summary;

    private List<DistributionItem> statusDistribution;

    private List<DistributionItem> categoryDistribution;

    private List<TypeSummary> typeSummaries;

    /**
     * 闪念统计总览指标。
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @Data
    public static class Summary {

        private long totalCount;

        private long weekNewCount;

        private long monthNewCount;

        private long pendingCount;

        private long doneCount;

        private long archivedCount;

        private BigDecimal conversionRate;

        private long backlogCount;

        private long highValueCount;
    }

    /**
     * 闪念统计分布项。
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @Data
    public static class DistributionItem {

        private String key;

        private String name;

        private long count;

        private BigDecimal percent;
    }

    /**
     * 闪念类型维度统计摘要。
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @Data
    public static class TypeSummary {

        private String thoughtType;

        private String typeName;

        private long totalCount;

        private List<DistributionItem> statusDistribution;

        private long backlogCount;

        private long doneCount;

        private long shelvedCount;

        private long archivedCount;

        private BigDecimal conversionRate;
    }
}
