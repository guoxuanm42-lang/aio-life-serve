package top.aiolife.llm.service.impl;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.aiolife.ai.langchain4j.AiModelFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 大模型兼容服务单元测试。
 *
 * @author Ethan
 * @date 2026-06-29
 */
@ExtendWith(MockitoExtension.class)
class LLMServiceTest {

    @Mock
    private AiModelFactory aiModelFactory;

    @InjectMocks
    private LLMServiceImpl llmService;

    /**
     * 验证非流式模型创建委托统一模型工厂。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void testGetChatModel() {
        String apiKey = "test-api-key";
        String baseUrl = "https://api.openai.com/v1";
        String modelName = "gpt-4";
        ChatModel expected = mock(ChatModel.class);

        when(aiModelFactory.createChatModel(apiKey, baseUrl, modelName, null)).thenReturn(expected);

        ChatModel model = llmService.getChatModel(apiKey, baseUrl, modelName);

        assertEquals(expected, model);
    }

    /**
     * 验证模型参数为空时仍由模型工厂统一处理。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void testGetChatModelWithInvalidParams() {
        ChatModel expected = mock(ChatModel.class);

        when(aiModelFactory.createChatModel(null, null, null, null)).thenReturn(expected);

        ChatModel model = llmService.getChatModel(null, null, null);

        assertEquals(expected, model);
    }

    /**
     * 验证带上下文生成回复时会拼接上下文和用户提示词。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void testGenerateResponseWithContext() {
        String apiKey = "test-api-key";
        String baseUrl = "https://api.openai.com/v1";
        String modelName = "gpt-4";
        String prompt = "Hello, how are you?";
        String context = "Previous conversation context";
        ChatModel model = mock(ChatModel.class);

        when(aiModelFactory.createChatModel(apiKey, baseUrl, modelName, null)).thenReturn(model);
        when(model.chat(context + "\n" + prompt)).thenReturn("response");

        String response = llmService.generateResponse(apiKey, baseUrl, modelName, prompt, context);

        assertEquals("response", response);
        verify(model).chat(context + "\n" + prompt);
    }

    /**
     * 验证无上下文生成回复时只发送用户提示词。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void testGenerateResponseWithoutContext() {
        String apiKey = "test-api-key";
        String baseUrl = "https://api.openai.com/v1";
        String modelName = "gpt-4";
        String prompt = "Hello, how are you?";
        ChatModel model = mock(ChatModel.class);

        when(aiModelFactory.createChatModel(apiKey, baseUrl, modelName, null)).thenReturn(model);
        when(model.chat(prompt)).thenReturn("response");

        String response = llmService.generateResponse(apiKey, baseUrl, modelName, prompt, null);

        assertEquals("response", response);
        verify(model).chat(prompt);
    }

    /**
     * 验证时迹总结会构建总结提示词并复用生成回复逻辑。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void testSummarizeTimeRecords() {
        String apiKey = "test-api-key";
        String baseUrl = "https://api.openai.com/v1";
        String modelName = "gpt-4";
        String timeRecords = "Test time records data";
        ChatModel model = mock(ChatModel.class);

        when(aiModelFactory.createChatModel(apiKey, baseUrl, modelName, null)).thenReturn(model);
        when(model.chat("请对以下时迹记录进行分析和总结，包括时间分配、活动类型分布、效率评价等方面，并给出合理的建议：\n" + timeRecords))
                .thenReturn("summary");

        String response = llmService.summarizeTimeRecords(apiKey, baseUrl, modelName, timeRecords);

        assertEquals("summary", response);
    }

    /**
     * 验证流式模型创建委托统一模型工厂。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void testGetStreamingChatModel() {
        StreamingChatModel expected = mock(StreamingChatModel.class);

        when(aiModelFactory.createStreamingChatModel("key", "url", "model", null)).thenReturn(expected);

        StreamingChatModel model = llmService.getStreamingChatModel("key", "url", "model");

        assertEquals(expected, model);
    }
}
