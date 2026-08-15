package top.aiolife.ai.activity.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 活动总结生成任务实体，保存幂等状态、模型结果和最终消息关联。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Data
@TableName("ai_activity_summary_generation")
public class AiActivitySummaryGenerationEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long conversationId;
    private String idempotencyKey;
    private String period;
    private String status;
    private String userMessage;
    private String content;
    private String modelName;
    private String agentCode;
    private String agentName;
    private Long userMessageId;
    private Long assistantMessageId;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
