package top.aiolife.ai.pojo.vo;

import lombok.Data;

/**
 * 统一 AI 入口的聊天响应对象。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
public class AiChatResp {

    /**
     * 本次聊天使用的 Agent 编码。
     */
    private String agentCode;

    /**
     * 用于绑定聊天消息的会话 id。
     */
    private Long conversationId;

    /**
     * 助手回复内容。
     */
    private String content;

    /**
     * 本次回复使用的模型名称。
     */
    private String modelName;

    /**
     * Agent 展示名称。
     */
    private String agentName;

    /**
     * 是否已将系统提示词应用到模型上下文。
     */
    private Boolean systemPromptApplied;
}
