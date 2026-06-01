package top.aiolife.record.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 美食记录统计视图，承载总览、趋势、分布、排行和提醒数据。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordStatisticsVO {

    private Overview overview;

    private List<TrendItem> frequencyTrend;

    private List<DistributionItem> categoryDistribution;

    private List<DistributionItem> mealTypeDistribution;

    private List<DistributionItem> statusDistribution;

    private List<RankItem> dishRank;

    private List<RankItem> ingredientRank;

    private List<RecordSummary> toImproveRecords;

    private List<RecordSummary> redoReminders;

    /**
     * 美食记录统计总览。
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Data
    public static class Overview {

        private long totalCount;

        private long monthCount;

        private BigDecimal averageRating;

        private BigDecimal averageTotalMinutes;

        private long worthRedoCount;

        private long toImproveCount;
    }

    /**
     * 美食记录趋势项。
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Data
    public static class TrendItem {

        private String label;

        private long count;
    }

    /**
     * 美食记录分布项。
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Data
    public static class DistributionItem {

        private String name;

        private long count;
    }

    /**
     * 美食记录排行项。
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Data
    public static class RankItem {

        private String name;

        private long count;
    }

    /**
     * 美食记录摘要项。
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Data
    public static class RecordSummary {

        private Long id;

        private String dishName;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate cookDate;

        private String category;

        private String mealType;

        private String status;

        private BigDecimal rating;

        private Integer totalMinutes;

        private String problems;

        private String nextImprove;

        private String summary;

        private Boolean worthRedo;
    }
}
