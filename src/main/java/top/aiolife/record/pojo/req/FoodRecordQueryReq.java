package top.aiolife.record.pojo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 美食记录分页查询请求。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordQueryReq {

    /**
     * 当前页码。
     */
    private Integer page = 1;

    /**
     * 每页数量。
     */
    private Integer pageSize = 50;

    /**
     * 菜名关键词。
     */
    private String keyword;

    /**
     * 分类。
     */
    private String category;

    /**
     * 餐次。
     */
    private String mealType;

    /**
     * 状态。
     */
    private String status;

    /**
     * 标签关键词。
     */
    private String tags;

    /**
     * 做饭开始日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 做饭结束日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
