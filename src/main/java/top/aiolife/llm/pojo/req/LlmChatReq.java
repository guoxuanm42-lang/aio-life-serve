package top.aiolife.llm.pojo.req;

import lombok.Data;

/**
 * 兼容旧版 LLM 聊天接口的请求对象。
 *
 * @author Ethan
 * @date 2026-07-20
 */
@Data
public class LlmChatReq {

    private String prompt;

    private String context;

    private Long conversationId;

    private String agentCode;
}
