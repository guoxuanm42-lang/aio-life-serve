package top.aiolife.llm.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * 大模型基础服务接口。
 *
 * @author Ethan
 * @date 2026-06-28
 */
public interface LLMService {

    /**
     * 获取使用默认参数的非流式聊天模型。
     *
     * @param apiKey 模型服务 API Key
     * @param baseUrl 模型服务基础地址
     * @param modelName 模型名称
     * @return LangChain4j 聊天模型
     *
     * @author Ethan
     * @date 2026-06-28
     */
    ChatModel getChatModel(String apiKey, String baseUrl, String modelName);

    /**
     * 获取可指定温度参数的非流式聊天模型。
     *
     * @param apiKey 模型服务 API Key
     * @param baseUrl 模型服务基础地址
     * @param modelName 模型名称
     * @param temperature 可选的模型温度参数
     * @return LangChain4j 聊天模型
     *
     * @author Ethan
     * @date 2026-06-28
     */
    ChatModel getChatModel(String apiKey, String baseUrl, String modelName, Double temperature);

    /**
     * 获取使用默认参数的流式聊天模型。
     *
     * @param apiKey 模型服务 API Key
     * @param baseUrl 模型服务基础地址
     * @param modelName 模型名称
     * @return LangChain4j 流式聊天模型
     *
     * @author Ethan
     * @date 2026-06-28
     */
    StreamingChatModel getStreamingChatModel(String apiKey, String baseUrl, String modelName);

    /**
     * 获取可指定温度参数的流式聊天模型。
     *
     * @param apiKey 模型服务 API Key
     * @param baseUrl 模型服务基础地址
     * @param modelName 模型名称
     * @param temperature 可选的模型温度参数
     * @return LangChain4j 流式聊天模型
     *
     * @author Ethan
     * @date 2026-06-28
     */
    StreamingChatModel getStreamingChatModel(String apiKey, String baseUrl, String modelName, Double temperature);

    /**
     * 使用旧接口参数生成非流式模型回复。
     *
     * @param apiKey 模型服务 API Key
     * @param baseUrl 模型服务基础地址
     * @param modelName 模型名称
     * @param prompt 用户提示词
     * @param context 可选上下文，会拼接到用户提示词前
     * @return 助手回复内容
     *
     * @author Ethan
     * @date 2026-06-28
     */
    String generateResponse(String apiKey, String baseUrl, String modelName, String prompt, String context);

    /**
     * 使用旧接口参数和温度参数生成非流式模型回复。
     *
     * @param apiKey 模型服务 API Key
     * @param baseUrl 模型服务基础地址
     * @param modelName 模型名称
     * @param prompt 用户提示词
     * @param context 可选上下文，会拼接到用户提示词前
     * @param temperature 可选的模型温度参数
     * @return 助手回复内容
     *
     * @author Ethan
     * @date 2026-06-28
     */
    String generateResponse(String apiKey, String baseUrl, String modelName, String prompt, String context, Double temperature);

    /**
     * 使用指定模型总结时间记录文本。
     *
     * @param apiKey 模型服务 API Key
     * @param baseUrl 模型服务基础地址
     * @param modelName 模型名称
     * @param timeRecords 时间记录文本
     * @return 总结内容
     *
     * @author Ethan
     * @date 2026-06-28
     */
    String summarizeTimeRecords(String apiKey, String baseUrl, String modelName, String timeRecords);
}