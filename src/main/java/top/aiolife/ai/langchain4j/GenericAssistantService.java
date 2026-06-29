package top.aiolife.ai.langchain4j;

import dev.langchain4j.service.UserMessage;

/**
 * 通用非流式 LangChain4j 助手服务接口。
 *
 * @author Ethan
 * @date 2026-06-28
 */
public interface GenericAssistantService {

    /**
     * 向已配置的助手发送一条用户消息。
     *
     * @param message 用户消息内容
     * @return 助手回复内容
     *
     * @author Ethan
     * @date 2026-06-28
     */
    String chat(@UserMessage String message);
}
