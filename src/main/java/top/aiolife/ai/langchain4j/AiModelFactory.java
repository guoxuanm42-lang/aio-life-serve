package top.aiolife.ai.langchain4j;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LangChain4j 聊天模型实例工厂。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Slf4j
@Component
public class AiModelFactory {

    /**
     * 创建 OpenAI 兼容的非流式聊天模型。
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
    public ChatModel createChatModel(String apiKey, String baseUrl, String modelName, Double temperature) {
        try {
            var builder = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName);
            if (temperature != null) {
                builder.temperature(temperature);
            }
            return builder.build();
        } catch (Exception e) {
            log.error("Failed to initialize chat model: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize chat model", e);
        }
    }

    /**
     * 创建 OpenAI 兼容的流式聊天模型。
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
    public StreamingChatModel createStreamingChatModel(String apiKey, String baseUrl, String modelName, Double temperature) {
        try {
            var builder = OpenAiStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName);
            if (temperature != null) {
                builder.temperature(temperature);
            }
            return builder.build();
        } catch (Exception e) {
            log.error("Failed to initialize streaming chat model: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize streaming chat model", e);
        }
    }
}
