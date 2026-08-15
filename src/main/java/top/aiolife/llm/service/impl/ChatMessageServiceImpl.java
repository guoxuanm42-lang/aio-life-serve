package top.aiolife.llm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.aiolife.llm.mapper.ChatMessageMapper;
import top.aiolife.llm.pojo.entity.ChatMessageEntity;
import top.aiolife.llm.service.ChatMessageService;

import java.util.List;

/**
 * AI 对话消息服务实现，使用 MyBatis-Plus 持久化普通聊天和业务来源消息。
 *
 * @author Ethan
 * @date 2026-08-15
 */
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessageEntity> implements ChatMessageService {

    /**
     * 查询当前用户的全部聊天消息，并使用消息 ID 保证同一创建时间下的顺序稳定。
     *
     * @param userId 当前用户 ID
     * @return 按创建时间和消息 ID 升序排列的消息列表
     *
     * @author Ethan
     * @date 2026-08-15
     */
    @Override
    public List<ChatMessageEntity> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getUserId, userId)
                .orderByAsc(ChatMessageEntity::getCreateTime)
                .orderByAsc(ChatMessageEntity::getId));
    }

    /**
     * 查询当前用户指定会话的聊天消息，并使用消息 ID 保证同一创建时间下的顺序稳定。
     *
     * @param userId 当前用户 ID
     * @param conversationId 会话 ID
     * @return 按创建时间和消息 ID 升序排列的会话消息列表
     *
     * @author Ethan
     * @date 2026-08-15
     */
    @Override
    public List<ChatMessageEntity> listByconversationId(Long userId, Long conversationId) {
        return this.list(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getUserId, userId)
                .eq(ChatMessageEntity::getConversationId, conversationId)
                .orderByAsc(ChatMessageEntity::getCreateTime)
                .orderByAsc(ChatMessageEntity::getId));
    }

    @Override
    public ChatMessageEntity saveMessage(Long userId, String role, String content, String modelName) {
        return saveMessage(userId, null, role, content, modelName);
    }

    @Override
    public ChatMessageEntity saveMessage(Long userId, Long conversationId, String role, String content, String modelName) {
        return saveMessage(userId, conversationId, role, content, modelName, null, null);
    }

    /**
     * 保存带业务来源和幂等标识的会话消息。
     *
     * @param userId 当前用户 ID
     * @param conversationId 会话 ID
     * @param role 消息角色
     * @param content 消息内容
     * @param modelName 模型名称
     * @param sourceType 业务来源类型
     * @param idempotencyKey 幂等键
     * @return 已保存的消息实体
     *
     * @author Ethan
     * @date 2026-08-14
     */
    @Override
    public ChatMessageEntity saveMessage(Long userId, Long conversationId, String role, String content,
                                         String modelName, String sourceType, String idempotencyKey) {
        ChatMessageEntity message = new ChatMessageEntity();
        message.setUserId(userId);
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setModelName(modelName);
        message.setSourceType(sourceType);
        message.setIdempotencyKey(idempotencyKey);
        message.fillCreateCommonField(userId);
        this.save(message);
        return message;
    }

    @Override
    public void deleteByUserId(Long userId) {
        this.remove(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getUserId, userId));
    }

    @Override
    public void deleteByconversationId(Long userId, Long conversationId) {
        this.remove(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getUserId, userId)
                .eq(ChatMessageEntity::getConversationId, conversationId));
    }
}
