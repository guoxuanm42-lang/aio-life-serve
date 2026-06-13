package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 闪念行动型详情保存请求。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Data
public class ThoughtActionDetailReq {

    private String resultSummary;

    private String reflection;

    private String nextAction;

    private String shelveReason;

    private String shelveReasonTag;

    private String restartPolicy;

    private String archiveReason;

    private String valueLevel;

    private String archiveType;
}
