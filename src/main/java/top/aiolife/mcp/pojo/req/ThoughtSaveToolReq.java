package top.aiolife.mcp.pojo.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import top.aiolife.mcp.annotation.McpField;
import top.aiolife.record.pojo.req.ThoughtActionDetailReq;
import top.aiolife.record.pojo.req.ThoughtEmotionDetailReq;
import top.aiolife.record.pojo.req.ThoughtReflectionDetailReq;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 思考（闪念）保存请求体（MCP Tool）
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtSaveToolReq {

    @JsonAlias({"topic"})
    @McpField(description = "主题/标题（可选，未传则从 content 第一行提炼）")
    private String subject;

    @McpField(description = "内容")
    private String content;

    @McpField(description = "主题色：blue/cyan/green/purple/pink/orange（可选）")
    private String themeKey;

    @McpField(description = "状态：pending/ongoing/done/shelved/archived（可选）")
    private String status;

    @McpField(description = "闪念类型：action 想法行动、emotion 情绪心情、reflection 复盘沉淀；不传默认 action")
    private String thoughtType;

    @McpField(description = "状态变化原因（可选），用于写入状态流转日志")
    private String changeReason;

    @JsonAlias({"recordTime", "happenedAt"})
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @McpField(description = "闪念创建时间/实际发生时间，格式 yyyy-MM-dd HH:mm:ss；recordTime 和 happenedAt 是该字段别名")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @McpField(description = "闪念更新时间，格式 yyyy-MM-dd HH:mm:ss（可选）")
    private LocalDateTime updateTime;

    @McpField(description = "行动型结构化详情；thoughtType=action 时生效")
    private ThoughtActionDetailReq actionDetail;

    @McpField(description = "情绪型结构化详情；thoughtType=emotion 时生效")
    private ThoughtEmotionDetailReq emotionDetail;

    @McpField(description = "复盘沉淀型结构化详情；thoughtType=reflection 时生效")
    private ThoughtReflectionDetailReq reflectionDetail;

    @McpField(description = "关联事件列表（可选）")
    private List<ThoughtSaveToolEventReq> events;

    @McpField(description = "幂等键（可选，建议 UUID；重复调用将只写入一次）")
    private String idempotencyKey;
}

