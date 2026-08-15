package top.aiolife.llm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.llm.pojo.entity.ChatMessageEntity;

import java.util.List;

/**
 * AI 对话消息服务，负责消息查询、保存和删除。
 *
 * @author Ethan
 * @date 2026-08-15
 */
public interface ChatMessageService extends IService<ChatMessageEntity> {

    /**
     * 查询当前用户的全部聊天消息。
     *
     * @param userId 当前用户 ID
     * @return 按创建时间和消息 ID 升序排列的消息列表
     *
     * @author Ethan
     * @date 2026-08-15
     */
    List<ChatMessageEntity> listByUserId(Long userId);

    /**
     * 查询当前用户指定会话的聊天消息。
     *
     * @param userId 当前用户 ID
     * @param conversationId 会话 ID
     * @return 按创建时间和消息 ID 升序排列的会话消息列表
     *
     * @author Ethan
     * @date 2026-08-15
     */
    List<ChatMessageEntity> listByconversationId(Long userId, Long conversationId);

    /**
     * 保存不绑定会话的普通消息。
     *
     * @param userId 当前用户 ID
     * @param role 消息角色
     * @param content 消息内容
     * @param modelName 模型名称
     * @return 已保存的消息实体
     *
     * @author Ethan
     * @date 2026-08-14
     */
    ChatMessageEntity saveMessage(Long userId, String role, String content, String modelName);

    /**
     * 保存指定会话的普通消息。
     *
     * @param userId 当前用户 ID
     * @param conversationId 会话 ID
     * @param role 消息角色
     * @param content 消息内容
     * @param modelName 模型名称
     * @return 已保存的消息实体
     *
     * @author Ethan
     * @date 2026-08-14
     */
    ChatMessageEntity saveMessage(Long userId, Long conversationId, String role, String content, String modelName);

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
    ChatMessageEntity saveMessage(Long userId, Long conversationId, String role, String content,
                                  String modelName, String sourceType, String idempotencyKey);

    /**
     * 删除当前用户的全部聊天消息。
     *
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-08-14
     */
    void deleteByUserId(Long userId);

    /**
     * 删除当前用户指定会话的全部消息。
     *
     * @param userId 当前用户 ID
     * @param conversationId 会话 ID
     *
     * @author Ethan
     * @date 2026-08-14
     */
    void deleteByconversationId(Long userId, Long conversationId);
}
