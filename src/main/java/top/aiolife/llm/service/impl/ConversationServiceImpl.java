package top.aiolife.llm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.ai.service.AiAgentConfigService;
import top.aiolife.llm.mapper.ChatSessionMapper;
import top.aiolife.llm.pojo.entity.ConversationEntity;
import top.aiolife.llm.service.ChatMessageService;
import top.aiolife.llm.service.ConversationService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 对话会话服务实现，统一处理助手绑定、归属校验和最近活动时间。
 *
 * @author Ethan
 * @date 2026-07-20
 */
@Service
@AllArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ChatSessionMapper, ConversationEntity> implements ConversationService {

    private static final String DEFAULT_AGENT_CODE = "life_assistant";

    private static final int MAX_TITLE_LENGTH = 100;

    private static final int MAX_AGENT_CODE_LENGTH = 64;

    private final ChatMessageService chatMessageService;

    private final AiAgentConfigService aiAgentConfigService;

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
    @Override
    public ConversationEntity createSession(Long userId, String title, String agentCode) {
        String safeTitle = normalizeTitle(title);
        String safeAgentCode = normalizeAgentCode(agentCode);
        AiAgentConfigVO agentConfig = aiAgentConfigService.getEffectiveConfig(userId, safeAgentCode);
        if (Boolean.FALSE.equals(agentConfig.getEnabled())) {
            throw new IllegalArgumentException("AI Agent 已禁用: " + safeAgentCode);
        }
        ConversationEntity session = new ConversationEntity();
        session.fillCreateCommonField(userId);
        session.setTitle(safeTitle);
        session.setAgentCode(safeAgentCode);
        this.save(session);
        return session;
    }

    /**
     * 查询当前用户的全部会话。
     *
     * @param userId 当前登录用户 id
     * @return 按最近更新时间倒序排列的会话列表
     *
     * @author Ethan
     * @date 2026-07-20
     */
    @Override
    public List<ConversationEntity> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<ConversationEntity>()
                .eq(ConversationEntity::getUserId, userId)
                .orderByDesc(ConversationEntity::getUpdateTime));
    }

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
    @Override
    public ConversationEntity getOwnedSession(Long userId, Long conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("会话 id 不能为空");
        }
        ConversationEntity session = this.getById(conversationId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在");
        }
        if (!java.util.Objects.equals(session.getUserId(), userId)) {
            throw new IllegalArgumentException("无权访问该会话");
        }
        return session;
    }

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
    @Override
    public void deleteSession(Long userId, Long conversationId) {
        ConversationEntity session = getOwnedSession(userId, conversationId);
        this.removeById(session.getId());
        chatMessageService.deleteByconversationId(userId, conversationId);
    }

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
    @Override
    public void updateTitle(Long userId, Long conversationId, String title) {
        ConversationEntity session = getOwnedSession(userId, conversationId);
        session.setTitle(normalizeTitle(title));
        session.setUpdateTime(LocalDateTime.now());
        session.setUpdateUser(userId);
        this.updateById(session);
    }

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
    @Override
    public void touchSession(Long userId, Long conversationId) {
        if (conversationId == null) {
            return;
        }
        ConversationEntity session = getOwnedSession(userId, conversationId);
        session.setUpdateTime(LocalDateTime.now());
        session.setUpdateUser(userId);
        this.updateById(session);
    }

    private String normalizeTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("会话标题不能为空");
        }
        String safeTitle = title.trim();
        if (safeTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("会话标题不能超过 100 个字符");
        }
        return safeTitle;
    }

    private String normalizeAgentCode(String agentCode) {
        String safeAgentCode = StringUtils.hasText(agentCode) ? agentCode.trim() : DEFAULT_AGENT_CODE;
        if (safeAgentCode.length() > MAX_AGENT_CODE_LENGTH) {
            throw new IllegalArgumentException("Agent 编码不能超过 64 个字符");
        }
        return safeAgentCode;
    }
}
