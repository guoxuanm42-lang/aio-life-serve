package top.aiolife.ai.langchain4j;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * 通用流式 LangChain4j 助手服务接口。
 *
 * @author Ethan
 * @date 2026-06-28
 */
public interface GenericStreamingAssistantService {

    /**
     * 向已配置的流式助手发送一条用户消息。
     *
     * @param message 用户消息内容
     * @return 助手回复的 token 流
     *
     * @author Ethan
     * @date 2026-06-28
     */
    TokenStream chat(@UserMessage String message);
}
