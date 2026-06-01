package top.aiolife.mcp.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 美食记录 MCP 查询返回摘要。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
public class FoodRecordQueryToolVO {

    private Long id;

    private String dishName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate cookDate;

    private String category;

    private String mealType;

    private Integer totalMinutes;

    private BigDecimal rating;

    private String summary;

    private String nextImprove;

    private String problems;

    private String tags;

    private String status;

    private Boolean worthRedo;
}
