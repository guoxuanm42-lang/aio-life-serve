package top.aiolife.record.pojo.req;

import lombok.Data;
import top.aiolife.mcp.annotation.McpField;

/**
 * 闪念复盘沉淀型详情保存请求。
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtReflectionDetailReq {

    @McpField(description = "复盘结论")
    private String reflectionSummary;

    @McpField(description = "经验/教训/方法/决策：experience/lesson/method/decision")
    private String lessonType;

    @McpField(description = "沉淀类型：experience/lesson/method/inspiration/decision")
    private String archiveType;

    @McpField(description = "价值等级：normal 普通、valuable 有价值、high 高价值")
    private String valueLevel;

    @McpField(description = "改进动作")
    private String improvementAction;

    @McpField(description = "关联项目")
    private String relatedProject;

    @McpField(description = "标签，多个标签可用逗号分隔")
    private String tags;
}
