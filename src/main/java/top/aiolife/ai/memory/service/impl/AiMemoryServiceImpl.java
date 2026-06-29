package top.aiolife.ai.memory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.memory.mapper.AiMemoryMapper;
import top.aiolife.ai.memory.pojo.entity.AiMemoryEntity;
import top.aiolife.ai.memory.pojo.req.AiMemorySaveReq;
import top.aiolife.ai.memory.pojo.vo.AiMemoryVO;
import top.aiolife.ai.memory.service.AiMemoryService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 长期记忆服务实现。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Service
@RequiredArgsConstructor
public class AiMemoryServiceImpl extends ServiceImpl<AiMemoryMapper, AiMemoryEntity> implements AiMemoryService {

    private static final String DEFAULT_AGENT_CODE = "life_assistant";

    private static final String DEFAULT_MEMORY_TYPE = "fact";

    private static final int DEFAULT_IMPORTANCE = 50;

    private final AiMemoryMapper aiMemoryMapper;

    /**
     * 查询当前用户的长期记忆列表。
     *
     * @param userId 当前登录用户 id
     * @param agentCode Agent 编码，可为空
     * @param memoryType 记忆类型，可为空
     * @return 长期记忆列表
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public List<AiMemoryVO> listMemories(Long userId, String agentCode, String memoryType) {
        LambdaQueryWrapper<AiMemoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMemoryEntity::getUserId, userId);
        if (StringUtils.hasText(agentCode)) {
            wrapper.eq(AiMemoryEntity::getAgentCode, agentCode.trim());
        }
        if (StringUtils.hasText(memoryType)) {
            wrapper.eq(AiMemoryEntity::getMemoryType, memoryType.trim());
        }
        wrapper.orderByDesc(AiMemoryEntity::getImportance)
                .orderByDesc(AiMemoryEntity::getUpdateTime);
        return aiMemoryMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    /**
     * 查询当前用户的单条长期记忆。
     *
     * @param userId 当前登录用户 id
     * @param id 记忆 id
     * @return 长期记忆视图对象
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public AiMemoryVO getMemory(Long userId, Long id) {
        return toVO(getOwnedEntity(userId, id));
    }

    /**
     * 保存当前用户的长期记忆。
     *
     * @param userId 当前登录用户 id
     * @param req 长期记忆保存请求
     * @return 保存后的长期记忆
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public AiMemoryVO saveMemory(Long userId, AiMemorySaveReq req) {
        AiMemorySaveReq safeReq = req == null ? new AiMemorySaveReq() : req;
        validateRequired(safeReq);
        LocalDateTime now = LocalDateTime.now();
        AiMemoryEntity entity = new AiMemoryEntity();
        entity.setUserId(userId);
        fillEditableFields(entity, safeReq);
        entity.setEnabled(true);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setIsDeleted(0);
        aiMemoryMapper.insert(entity);
        return toVO(entity);
    }

    /**
     * 更新当前用户的长期记忆。
     *
     * @param userId 当前登录用户 id
     * @param id 记忆 id
     * @param req 长期记忆保存请求
     * @return 更新后的长期记忆
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public AiMemoryVO updateMemory(Long userId, Long id, AiMemorySaveReq req) {
        AiMemorySaveReq safeReq = req == null ? new AiMemorySaveReq() : req;
        validateRequired(safeReq);
        AiMemoryEntity entity = getOwnedEntity(userId, id);
        fillEditableFields(entity, safeReq);
        entity.setUpdateTime(LocalDateTime.now());
        aiMemoryMapper.updateById(entity);
        return toVO(entity);
    }

    /**
     * 更新当前用户长期记忆的启用状态。
     *
     * @param userId 当前登录用户 id
     * @param id 记忆 id
     * @param enabled 是否启用
     * @return 更新后的长期记忆
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public AiMemoryVO updateStatus(Long userId, Long id, Boolean enabled) {
        if (enabled == null) {
            throw new IllegalArgumentException("enabled 不能为空");
        }
        AiMemoryEntity entity = getOwnedEntity(userId, id);
        entity.setEnabled(enabled);
        entity.setUpdateTime(LocalDateTime.now());
        aiMemoryMapper.updateById(entity);
        return toVO(entity);
    }

    /**
     * 查询聊天时可注入的长期记忆。
     *
     * @param userId 当前登录用户 id
     * @param agentCode Agent 编码
     * @param maxMemoryItems 最大记忆条数
     * @return 可注入的长期记忆列表
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public List<AiMemoryVO> listEffectiveMemories(Long userId, String agentCode, Integer maxMemoryItems) {
        if (maxMemoryItems == null || maxMemoryItems <= 0) {
            return List.of();
        }
        LambdaQueryWrapper<AiMemoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMemoryEntity::getUserId, userId)
                .eq(AiMemoryEntity::getAgentCode, normalizeAgentCode(agentCode))
                .eq(AiMemoryEntity::getEnabled, true)
                .orderByDesc(AiMemoryEntity::getImportance)
                .orderByDesc(AiMemoryEntity::getUpdateTime)
                .last("limit " + maxMemoryItems);
        return aiMemoryMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    private AiMemoryEntity getOwnedEntity(Long userId, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("记忆 id 不能为空");
        }
        LambdaQueryWrapper<AiMemoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMemoryEntity::getId, id)
                .eq(AiMemoryEntity::getUserId, userId)
                .last("limit 1");
        AiMemoryEntity entity = aiMemoryMapper.selectOne(wrapper);
        if (entity == null) {
            throw new IllegalArgumentException("记忆不存在或不属于当前用户");
        }
        return entity;
    }

    private void validateRequired(AiMemorySaveReq req) {
        if (!StringUtils.hasText(req.getMemoryKey())) {
            throw new IllegalArgumentException("memoryKey 不能为空");
        }
        if (!StringUtils.hasText(req.getMemoryValue())) {
            throw new IllegalArgumentException("memoryValue 不能为空");
        }
    }

    private void fillEditableFields(AiMemoryEntity entity, AiMemorySaveReq req) {
        entity.setAgentCode(normalizeAgentCode(req.getAgentCode()));
        entity.setMemoryType(StringUtils.hasText(req.getMemoryType()) ? req.getMemoryType().trim() : DEFAULT_MEMORY_TYPE);
        entity.setMemoryKey(req.getMemoryKey().trim());
        entity.setMemoryValue(req.getMemoryValue().trim());
        entity.setSourceType(normalizeBlank(req.getSourceType()));
        entity.setSourceId(normalizeBlank(req.getSourceId()));
        entity.setImportance(req.getImportance() == null ? DEFAULT_IMPORTANCE : req.getImportance());
    }

    private String normalizeAgentCode(String agentCode) {
        return StringUtils.hasText(agentCode) ? agentCode.trim() : DEFAULT_AGENT_CODE;
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private AiMemoryVO toVO(AiMemoryEntity entity) {
        AiMemoryVO vo = new AiMemoryVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setAgentCode(entity.getAgentCode());
        vo.setMemoryType(entity.getMemoryType());
        vo.setMemoryKey(entity.getMemoryKey());
        vo.setMemoryValue(entity.getMemoryValue());
        vo.setSourceType(entity.getSourceType());
        vo.setSourceId(entity.getSourceId());
        vo.setImportance(entity.getImportance());
        vo.setEnabled(entity.getEnabled());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
