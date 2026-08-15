package top.aiolife.llm.pojo.req;

import lombok.Data;

/**
 * 更新 AI 对话会话标题的请求对象。
 *
 * @author Ethan
 * @date 2026-07-20
 */
@Data
public class ConversationUpdateReq {

    private String title;
}
