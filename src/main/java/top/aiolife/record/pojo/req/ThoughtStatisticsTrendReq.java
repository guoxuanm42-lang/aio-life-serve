package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 闪念统计趋势查询请求。
 *
 * <p>用途：承载趋势统计的时间范围、分组方式、分类和状态筛选条件。</p>
 *
 * @author Ethan
 * @date 2026-06-10
 */
@Data
public class ThoughtStatisticsTrendReq {

    private String range;

    private String groupBy;

    private String category;

    private String status;
}
