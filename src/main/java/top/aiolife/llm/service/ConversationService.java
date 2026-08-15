package top.aiolife.llm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.llm.pojo.entity.ConversationEntity;

import java.util.List;

/**
 * AI 对话会话服务，负责会话创建、归属校验和生命周期管理。
 *
 * @author Ethan
 * @date 2026-07-20
 */
public interface ConversationService extends IService<ConversationEntity> {

    /**
     * 为当前用户和指定助手创建会话。
     *
     * @param userId 当前登录用户 id
     * @param title 会话标题
     * @param agentCode 绑定的 Agent 编码，为空时使用生活总助理
     * @return 创建后的会话实体
     *
     * @author Ethan
     * @date 2026-07-20
     */
    ConversationEntity createSession(Long userId, String title, String agentCode);

    /**
     * 查询当前用户的全部会话。
     *
     * @param userId 当前登录用户 id
     * @return 按最近更新时间倒序排列的会话列表
     *
     * @author Ethan
     * @date 2026-07-20
     */
    List<ConversationEntity> listByUserId(Long userId);

    /**
     * 查询会话并校验其属于当前用户。
     *
     * @param userId 当前登录用户 id
     * @param conversationId 会话 id
     * @return 当前用户拥有的会话
     * @throws IllegalArgumentException 会话不存在或不属于当前用户时抛出
     *
     * @author Ethan
     * @date 2026-07-20
     */
    ConversationEntity getOwnedSession(Long userId, Long conversationId);

    /**
     * 删除当前用户拥有的会话及其消息。
     *
     * @param userId 当前登录用户 id
     * @param conversationId 会话 id
     * @throws IllegalArgumentException 会话不存在或不属于当前用户时抛出
     *
     * @author Ethan
     * @date 2026-07-20
     */
    void deleteSession(Long userId, Long conversationId);

    /**
     * 更新当前用户拥有的会话标题。
     *
     * @param userId 当前登录用户 id
     * @param conversationId 会话 id
     * @param title 新标题
     * @throws IllegalArgumentException 会话不存在、越权或标题不合法时抛出
     *
     * @author Ethan
     * @date 2026-07-20
     */
    void updateTitle(Long userId, Long conversationId, String title);

    /**
     * 更新当前用户会话的最近活动时间。
     *
     * @param userId 当前登录用户 id
     * @param conversationId 会话 id
     * @throws IllegalArgumentException 会话不存在或不属于当前用户时抛出
     *
     * @author Ethan
     * @date 2026-07-20
     */
    void touchSession(Long userId, Long conversationId);
}
