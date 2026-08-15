package top.aiolife.ai.service.impl;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.TokenStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.ai.langchain4j.AiChatMemoryFactory;
import top.aiolife.ai.langchain4j.AiPromptContextBuilder;
import top.aiolife.ai.langchain4j.AiServiceFactory;
import top.aiolife.ai.langchain4j.AiServiceRuntime;
import top.aiolife.ai.langchain4j.GenericAssistantService;
import top.aiolife.ai.langchain4j.GenericStreamingAssistantService;
import top.aiolife.ai.memory.pojo.vo.AiMemoryVO;
import top.aiolife.ai.memory.service.AiMemoryService;
import top.aiolife.ai.pojo.req.AiChatReq;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.ai.pojo.vo.AiChatResp;
import top.aiolife.ai.service.AiAgentConfigService;
import top.aiolife.ai.tool.AiToolService;
import top.aiolife.llm.pojo.entity.ConversationEntity;
import top.aiolife.llm.pojo.entity.LLMKeyEntity;
import top.aiolife.llm.service.ChatMessageService;
import top.aiolife.llm.service.ConversationService;
import top.aiolife.llm.service.LLMKeyService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 聊天编排服务单元测试。
 *
 * @author Ethan
 * @date 2026-07-23
 */
class AiChatServiceImplTest {

    private static final Long USER_ID = 1001L;

    /**
     * 验证非流式聊天使用默认 Agent，并只保存用户原始输入。
     *
     * @author Ethan
     * @date 2026-07-23
     */
    @Test
    void shouldChatWithDefaultAgentAndSaveRawUserMessage() {
        TestContext context = new TestContext();
        AiChatReq req = new AiChatReq();
        req.setMessage("你好");
        req.setContext("旧上下文");
        req.setConversationId(10L);
        AiAgentConfigVO agent = buildAgent("life_assistant", true, null);
        LLMKeyEntity llmKey = buildLlmKey("default-key");
        AiMemoryVO memory = buildMemory("偏好", "简洁");
        ChatMemory chatMemory = mock(ChatMemory.class);
        GenericAssistantService assistantService = mock(GenericAssistantService.class);
        AiServiceRuntime runtime = buildRuntime("life_assistant", assistantService, mock(GenericStreamingAssistantService.class));

        when(context.aiAgentConfigService.getEffectiveConfig(USER_ID, "life_assistant")).thenReturn(agent);
        when(context.conversationService.getOwnedSession(USER_ID, 10L))
                .thenReturn(buildConversation("life_assistant"));
        when(context.llmKeyService.getDefaultLLMKey(USER_ID)).thenReturn(llmKey);
        when(context.aiMemoryService.listEffectiveMemories(USER_ID, "life_assistant", 2)).thenReturn(List.of(memory));
        when(context.aiPromptContextBuilder.buildSystemMessage("系统提示", List.of(memory), "旧上下文")).thenReturn("系统上下文");
        when(context.aiChatMemoryFactory.createMemory(USER_ID, "life_assistant", 10L, 8)).thenReturn(chatMemory);
        when(context.aiToolService.buildTools(USER_ID, agent)).thenReturn(Map.of());
        when(context.aiServiceFactory.createRuntime(eq(agent), eq(llmKey), eq("系统上下文"), eq(chatMemory), eq(Map.of()), eq(0.7))).thenReturn(runtime);
        when(assistantService.chat("你好")).thenReturn("你好，有什么可以帮你？");

        AiChatResp result = context.service.chat(USER_ID, req);

        assertEquals("life_assistant", result.getAgentCode());
        assertEquals("你好，有什么可以帮你？", result.getContent());
        verify(context.chatMessageService).saveMessage(USER_ID, 10L, "user", "你好", "gpt-4");
        verify(context.chatMessageService).saveMessage(USER_ID, 10L, "assistant", "你好，有什么可以帮你？", "gpt-4");
    }

    /**
     * 验证指定 Agent 会按请求编码获取配置。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldChatWithSpecifiedAgentCode() {
        TestContext context = new TestContext();
        AiChatReq req = new AiChatReq();
        req.setAgentCode("coding_assistant");
        req.setMessage("解释代码");
        AiAgentConfigVO agent = buildAgent("coding_assistant", true, "key-1");
        LLMKeyEntity llmKey = buildLlmKey("key-1");
        GenericAssistantService assistantService = mock(GenericAssistantService.class);
        AiServiceRuntime runtime = buildRuntime("coding_assistant", assistantService, mock(GenericStreamingAssistantService.class));

        when(context.aiAgentConfigService.getEffectiveConfig(USER_ID, "coding_assistant")).thenReturn(agent);
        when(context.llmKeyService.getLLMKeyList(USER_ID)).thenReturn(List.of(llmKey));
        when(context.aiMemoryService.listEffectiveMemories(USER_ID, "coding_assistant", 2)).thenReturn(List.of());
        when(context.aiPromptContextBuilder.buildSystemMessage("系统提示", List.of(), null)).thenReturn("系统上下文");
        when(context.aiChatMemoryFactory.createMemory(USER_ID, "coding_assistant", null, 8)).thenReturn(null);
        when(context.aiToolService.buildTools(USER_ID, agent)).thenReturn(Map.of());
        when(context.aiServiceFactory.createRuntime(eq(agent), eq(llmKey), eq("系统上下文"), any(), eq(Map.of()), eq(0.7))).thenReturn(runtime);
        when(assistantService.chat("解释代码")).thenReturn("代码说明");

        AiChatResp result = context.service.chat(USER_ID, req);

        assertEquals("coding_assistant", result.getAgentCode());
        verify(context.aiAgentConfigService).getEffectiveConfig(USER_ID, "coding_assistant");
        verify(context.chatMessageService).saveMessage(USER_ID, null, "user", "解释代码", "gpt-4");
    }

    /**
     * 验证缺少默认模型 Key 时抛出明确异常。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldRejectMissingDefaultLlmKey() {
        TestContext context = new TestContext();
        AiChatReq req = new AiChatReq();
        req.setMessage("你好");

        when(context.aiAgentConfigService.getEffectiveConfig(USER_ID, "life_assistant")).thenReturn(buildAgent("life_assistant", true, null));
        when(context.llmKeyService.getDefaultLLMKey(USER_ID)).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> context.service.chat(USER_ID, req));

        assertEquals("请先配置默认大模型 API Key", exception.getMessage());
    }

    /**
     * 验证禁用 Agent 时抛出明确异常。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldRejectDisabledAgent() {
        TestContext context = new TestContext();
        AiChatReq req = new AiChatReq();
        req.setAgentCode("coding_assistant");
        req.setMessage("你好");

        when(context.aiAgentConfigService.getEffectiveConfig(USER_ID, "coding_assistant")).thenReturn(buildAgent("coding_assistant", false, null));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> context.service.chat(USER_ID, req));

        assertEquals("AI Agent 已禁用: coding_assistant", exception.getMessage());
    }

    /**
     * 验证指定模型 Key 不属于当前用户时抛出明确异常。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldRejectModelKeyNotOwnedByUser() {
        TestContext context = new TestContext();
        AiChatReq req = new AiChatReq();
        req.setMessage("你好");

        when(context.aiAgentConfigService.getEffectiveConfig(USER_ID, "life_assistant")).thenReturn(buildAgent("life_assistant", true, "missing-key"));
        when(context.llmKeyService.getLLMKeyList(USER_ID)).thenReturn(List.of(buildLlmKey("owned-key")));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> context.service.chat(USER_ID, req));

        assertEquals("模型配置不存在或不属于当前用户", exception.getMessage());
    }

    /**
     * 验证流式入口保存用户原始输入并调用流式服务。
     *
     * @author Ethan
     * @date 2026-07-23
     */
    @Test
    void shouldStartStreamingChatAndSaveRawUserMessage() {
        TestContext context = new TestContext();
        AiChatReq req = new AiChatReq();
        req.setMessage("流式消息");
        req.setContext("旧上下文");
        req.setConversationId(20L);
        AiAgentConfigVO agent = buildAgent("life_assistant", true, null);
        LLMKeyEntity llmKey = buildLlmKey("default-key");
        GenericStreamingAssistantService streamingService = mock(GenericStreamingAssistantService.class);
        TokenStream tokenStream = mock(TokenStream.class);
        AiServiceRuntime runtime = buildRuntime("life_assistant", mock(GenericAssistantService.class), streamingService);

        when(context.aiAgentConfigService.getEffectiveConfig(USER_ID, "life_assistant")).thenReturn(agent);
        when(context.conversationService.getOwnedSession(USER_ID, 20L))
                .thenReturn(buildConversation("life_assistant"));
        when(context.llmKeyService.getDefaultLLMKey(USER_ID)).thenReturn(llmKey);
        when(context.aiMemoryService.listEffectiveMemories(USER_ID, "life_assistant", 2)).thenReturn(List.of());
        when(context.aiPromptContextBuilder.buildSystemMessage("系统提示", List.of(), "旧上下文")).thenReturn("系统上下文");
        when(context.aiChatMemoryFactory.createMemory(USER_ID, "life_assistant", 20L, 8)).thenReturn(null);
        when(context.aiToolService.buildTools(USER_ID, agent)).thenReturn(Map.of());
        when(context.aiServiceFactory.createRuntime(eq(agent), eq(llmKey), eq("系统上下文"), any(), eq(Map.of()), eq(0.7))).thenReturn(runtime);
        when(streamingService.chat("流式消息")).thenReturn(tokenStream);
        when(tokenStream.onPartialResponse(any(Consumer.class))).thenReturn(tokenStream);
        when(tokenStream.onCompleteResponse(any(Consumer.class))).thenReturn(tokenStream);
        when(tokenStream.onError(any(Consumer.class))).thenReturn(tokenStream);

        context.service.chatStream(USER_ID, req);

        verify(context.chatMessageService).saveMessage(USER_ID, 20L, "user", "流式消息", "gpt-4");
        verify(streamingService).chat("流式消息");
        verify(tokenStream).start();

        ArgumentCaptor<Consumer<String>> partialResponseCaptor = ArgumentCaptor.forClass(Consumer.class);
        ArgumentCaptor<Consumer> completeResponseCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(tokenStream).onPartialResponse(partialResponseCaptor.capture());
        verify(tokenStream).onCompleteResponse(completeResponseCaptor.capture());

        partialResponseCaptor.getValue().accept("  代码\n");
        completeResponseCaptor.getValue().accept(null);

        verify(context.chatMessageService).saveMessage(USER_ID, 20L, "assistant", "  代码\n", "gpt-4");
    }

    /**
     * 验证流式生成失败时不保存不完整的助手消息。
     *
     * @author Ethan
     * @date 2026-07-23
     */
    @Test
    void shouldNotSavePartialAssistantMessageWhenStreamingFails() {
        TestContext context = new TestContext();
        AiChatReq req = new AiChatReq();
        req.setMessage("流式消息");
        req.setConversationId(21L);
        AiAgentConfigVO agent = buildAgent("life_assistant", true, null);
        LLMKeyEntity llmKey = buildLlmKey("default-key");
        GenericStreamingAssistantService streamingService = mock(GenericStreamingAssistantService.class);
        TokenStream tokenStream = mock(TokenStream.class);
        AiServiceRuntime runtime = buildRuntime("life_assistant", mock(GenericAssistantService.class), streamingService);

        when(context.aiAgentConfigService.getEffectiveConfig(USER_ID, "life_assistant")).thenReturn(agent);
        when(context.conversationService.getOwnedSession(USER_ID, 21L))
                .thenReturn(buildConversation("life_assistant"));
        when(context.llmKeyService.getDefaultLLMKey(USER_ID)).thenReturn(llmKey);
        when(context.aiMemoryService.listEffectiveMemories(USER_ID, "life_assistant", 2)).thenReturn(List.of());
        when(context.aiPromptContextBuilder.buildSystemMessage("系统提示", List.of(), null)).thenReturn("系统上下文");
        when(context.aiChatMemoryFactory.createMemory(USER_ID, "life_assistant", 21L, 8)).thenReturn(null);
        when(context.aiToolService.buildTools(USER_ID, agent)).thenReturn(Map.of());
        when(context.aiServiceFactory.createRuntime(eq(agent), eq(llmKey), eq("系统上下文"), any(), eq(Map.of()), eq(0.7))).thenReturn(runtime);
        when(streamingService.chat("流式消息")).thenReturn(tokenStream);
        when(tokenStream.onPartialResponse(any(Consumer.class))).thenReturn(tokenStream);
        when(tokenStream.onCompleteResponse(any(Consumer.class))).thenReturn(tokenStream);
        when(tokenStream.onError(any(Consumer.class))).thenReturn(tokenStream);

        context.service.chatStream(USER_ID, req);

        ArgumentCaptor<Consumer<String>> partialResponseCaptor = ArgumentCaptor.forClass(Consumer.class);
        ArgumentCaptor<Consumer<Throwable>> errorCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(tokenStream).onPartialResponse(partialResponseCaptor.capture());
        verify(tokenStream).onError(errorCaptor.capture());

        partialResponseCaptor.getValue().accept("未完成");
        errorCaptor.getValue().accept(new IllegalStateException("供应商错误"));

        verify(context.chatMessageService, never())
                .saveMessage(USER_ID, 21L, "assistant", "未完成", "gpt-4");
    }

    private AiAgentConfigVO buildAgent(String code, Boolean enabled, String modelKeyId) {
        AiAgentConfigVO vo = new AiAgentConfigVO();
        vo.setCode(code);
        vo.setName(code);
        vo.setSystemPrompt("系统提示");
        vo.setModelKeyId(modelKeyId);
        vo.setMaxContextMessages(8);
        vo.setMaxMemoryItems(2);
        vo.setTemperature(BigDecimal.valueOf(0.7));
        vo.setEnabled(enabled);
        return vo;
    }

    private LLMKeyEntity buildLlmKey(String id) {
        LLMKeyEntity entity = new LLMKeyEntity();
        entity.setId(id);
        entity.setUserId(USER_ID);
        entity.setModelName("gpt-4");
        entity.setApiKey("api-key");
        entity.setBaseUrl("https://example.com/v1");
        return entity;
    }

    private ConversationEntity buildConversation(String agentCode) {
        ConversationEntity entity = new ConversationEntity();
        entity.setAgentCode(agentCode);
        return entity;
    }

    private AiMemoryVO buildMemory(String key, String value) {
        AiMemoryVO vo = new AiMemoryVO();
        vo.setMemoryKey(key);
        vo.setMemoryValue(value);
        return vo;
    }

    private AiServiceRuntime buildRuntime(String agentCode,
                                          GenericAssistantService assistantService,
                                          GenericStreamingAssistantService streamingAssistantService) {
        return AiServiceRuntime.builder()
                .agentCode(agentCode)
                .agentName(agentCode)
                .modelName("gpt-4")
                .systemPromptApplied(true)
                .assistantService(assistantService)
                .streamingAssistantService(streamingAssistantService)
                .build();
    }

    private static class TestContext {

        private final LLMKeyService llmKeyService = mock(LLMKeyService.class);

        private final ChatMessageService chatMessageService = mock(ChatMessageService.class);

        private final ConversationService conversationService = mock(ConversationService.class);

        private final AiAgentConfigService aiAgentConfigService = mock(AiAgentConfigService.class);

        private final AiServiceFactory aiServiceFactory = mock(AiServiceFactory.class);

        private final AiChatMemoryFactory aiChatMemoryFactory = mock(AiChatMemoryFactory.class);

        private final AiPromptContextBuilder aiPromptContextBuilder = mock(AiPromptContextBuilder.class);

        private final AiMemoryService aiMemoryService = mock(AiMemoryService.class);

        private final AiToolService aiToolService = mock(AiToolService.class);

        private final AiChatServiceImpl service = new AiChatServiceImpl(
                llmKeyService,
                chatMessageService,
                conversationService,
                aiAgentConfigService,
                aiServiceFactory,
                aiChatMemoryFactory,
                aiPromptContextBuilder,
                aiMemoryService,
                aiToolService
        );
    }
}
