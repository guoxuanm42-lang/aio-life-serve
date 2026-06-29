package top.aiolife.llm.service.impl;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.aiolife.ai.langchain4j.AiModelFactory;
import top.aiolife.llm.service.LLMService;

/**
 * 兼容旧接口的大模型服务实现，底层复用统一 AI 模型工厂。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMServiceImpl implements LLMService {

    private final AiModelFactory aiModelFactory;

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
    @Override
    public ChatModel getChatModel(String apiKey, String baseUrl, String modelName) {
        return getChatModel(apiKey, baseUrl, modelName, null);
    }

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
    @Override
    public ChatModel getChatModel(String apiKey, String baseUrl, String modelName, Double temperature) {
        return aiModelFactory.createChatModel(apiKey, baseUrl, modelName, temperature);
    }

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
    @Override
    public StreamingChatModel getStreamingChatModel(String apiKey, String baseUrl, String modelName) {
        return getStreamingChatModel(apiKey, baseUrl, modelName, null);
    }

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
    @Override
    public StreamingChatModel getStreamingChatModel(String apiKey, String baseUrl, String modelName, Double temperature) {
        return aiModelFactory.createStreamingChatModel(apiKey, baseUrl, modelName, temperature);
    }

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
    @Override
    public String generateResponse(String apiKey, String baseUrl, String modelName, String prompt, String context) {
        return generateResponse(apiKey, baseUrl, modelName, prompt, context, null);
    }

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
    @Override
    public String generateResponse(String apiKey, String baseUrl, String modelName, String prompt, String context, Double temperature) {
        try {
            ChatModel model = getChatModel(apiKey, baseUrl, modelName, temperature);
            String fullPrompt = context != null && !context.isEmpty() ? context + "\n" + prompt : prompt;
            return model.chat(fullPrompt);
        } catch (Exception e) {
            log.error("Failed to generate response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate response", e);
        }
    }

    /**
     * 使用指定模型总结时间记录文本。
     *
            String prompt = "请对以下时间记录进行分析和总结，包括时间分配、活动类型分布、效率评价等方面，并给出合理的建议：\n" + timeRecords;
     * @param baseUrl 模型服务基础地址
     * @param modelName 模型名称
     * @param timeRecords 时间记录文本
     * @return 总结内容
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public String summarizeTimeRecords(String apiKey, String baseUrl, String modelName, String timeRecords) {
        try {
            String prompt = "请对以下时迹记录进行分析和总结，包括时间分配、活动类型分布、效率评价等方面，并给出合理的建议：\n" + timeRecords;
            return generateResponse(apiKey, baseUrl, modelName, prompt, null);
        } catch (Exception e) {
            log.error("Failed to summarize time records: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to summarize time records", e);
        }
    }
}
