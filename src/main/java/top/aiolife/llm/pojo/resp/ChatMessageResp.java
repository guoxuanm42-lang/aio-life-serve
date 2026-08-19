package top.aiolife.llm.pojo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;

import java.time.LocalDateTime;

/**
 * 聊天历史消息响应，兼容原消息字段并可附带活动总结结构化快照。
 *
 * @author Ethan
 * @date 2026-08-15
 */
@Data
@Builder
public class ChatMessageResp {

    private Long id;
    private Long userId;
    private Long conversationId;
    private String role;
    private String content;
    private String modelName;
    private String sourceType;
    private String idempotencyKey;
    private AiActivitySummaryContext activitySummary;
    private Long createUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Long updateUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
