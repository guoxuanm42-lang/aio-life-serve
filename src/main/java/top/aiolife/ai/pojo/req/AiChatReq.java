package top.aiolife.ai.pojo.req;

import lombok.Data;

/**
 * 统一 AI 入口的聊天请求对象。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
public class AiChatReq {

    /**
     * Agent 编码，用于选择配置化智能体。
     */
    private String agentCode;

    /**
     * 会话 id，用于绑定保存的聊天消息。
     */
    private Long conversationId;

    /**
     * 发送给模型的用户消息。
     */
    private String message;

    /**
     * 兼容旧前端逻辑保留的上下文。
     */
    private String context;
}
