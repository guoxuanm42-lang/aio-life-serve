package top.aiolife.record.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 闪念行动型详情保存请求。
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtActionDetailReq {

    @McpField(description = "处理结果：最终做了什么")
    private String resultSummary;

    @McpField(description = "心得/复盘：过程中学到了什么")
    private String reflection;

    @McpField(description = "后续动作：是否还有下一步")
    private String nextAction;

    @McpField(description = "搁置原因：为什么暂时不做")
    private String shelveReason;

    @McpField(description = "搁置原因标签：unrealistic/no_time/low_value/blocked/duplicate/other")
    private String shelveReasonTag;

    @McpField(description = "是否可重启：no/later/conditional")
    private String restartPolicy;

    @McpField(description = "归档原因：为什么值得长期保存")
    private String archiveReason;

    @McpField(description = "价值等级：normal 普通、valuable 有价值、high 高价值")
    private String valueLevel;

    @McpField(description = "沉淀类型：experience/lesson/method/inspiration/decision")
    private String archiveType;
}
