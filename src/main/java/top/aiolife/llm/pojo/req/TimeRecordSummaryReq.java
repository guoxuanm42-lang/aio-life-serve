package top.aiolife.llm.pojo.req;

import lombok.Data;

/**
 * 生成时迹总结的请求对象。
 *
 * @author Ethan
 * @date 2026-07-20
 */
@Data
public class TimeRecordSummaryReq {

    private String type;

    private Long conversationId;
}
