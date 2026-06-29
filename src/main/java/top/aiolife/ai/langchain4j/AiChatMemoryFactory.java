package top.aiolife.ai.langchain4j;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.llm.pojo.entity.ChatMessageEntity;
import top.aiolife.llm.service.ChatMessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI 短期聊天记忆工厂。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Component
@RequiredArgsConstructor
public class AiChatMemoryFactory {

    private static final int DEFAULT_MAX_CONTEXT_MESSAGES = 10;

    private final ChatMessageService chatMessageService;

    /**
     * 根据会话历史创建 LangChain4j 短期聊天记忆。
     *
     * @param userId 当前登录用户 id
     * @param agentCode Agent 编码
     * @param conversationId 会话 id，可为空
     * @param maxContextMessages 最大上下文消息数量
     * @return LangChain4j 聊天记忆对象
     *
     * @author Ethan
     * @date 2026-06-28
     */
    public ChatMemory createMemory(Long userId, String agentCode, Long conversationId, Integer maxContextMessages) {
        int maxMessages = normalizeMaxMessages(maxContextMessages);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(buildMemoryId(userId, agentCode, conversationId))
                .maxMessages(maxMessages)
                .build();
        chatMemory.set(loadRecentMessages(userId, conversationId, maxMessages));
        return chatMemory;
    }

    private String buildMemoryId(Long userId, String agentCode, Long conversationId) {
        String safeAgentCode = StringUtils.hasText(agentCode) ? agentCode.trim() : "life_assistant";
        String safeConversationId = conversationId == null ? "temp-" + UUID.randomUUID() : String.valueOf(conversationId);
        return userId + ":" + safeAgentCode + ":" + safeConversationId;
    }

    private int normalizeMaxMessages(Integer maxContextMessages) {
        return maxContextMessages == null || maxContextMessages <= 0 ? DEFAULT_MAX_CONTEXT_MESSAGES : maxContextMessages;
    }

    private List<ChatMessage> loadRecentMessages(Long userId, Long conversationId, int maxMessages) {
        if (conversationId == null) {
            return List.of();
        }
        List<ChatMessageEntity> history = chatMessageService.listByconversationId(userId, conversationId);
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        int fromIndex = Math.max(0, history.size() - maxMessages);
        List<ChatMessage> messages = new ArrayList<>();
        for (ChatMessageEntity entity : history.subList(fromIndex, history.size())) {
            ChatMessage message = toChatMessage(entity);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    private ChatMessage toChatMessage(ChatMessageEntity entity) {
        if (entity == null || !StringUtils.hasText(entity.getRole()) || !StringUtils.hasText(entity.getContent())) {
            return null;
        }
        String role = entity.getRole().trim();
        String content = entity.getContent().trim();
        if ("user".equals(role)) {
            return UserMessage.from(content);
        }
        if ("assistant".equals(role)) {
            return AiMessage.from(content);
        }
        return null;
    }
}
