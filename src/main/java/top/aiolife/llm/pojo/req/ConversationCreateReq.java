package top.aiolife.llm.pojo.req;

import lombok.Data;

/**
 * 创建 AI 对话会话的请求对象。
 *
 * @author Ethan
 * @date 2026-07-20
 */
@Data
public class ConversationCreateReq {

    private String title;

    private String agentCode;
}
