package top.aiolife.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.mapper.AiAgentConfigMapper;
import top.aiolife.ai.pojo.entity.AiAgentConfigEntity;
import top.aiolife.ai.pojo.req.AiAgentConfigSaveReq;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.ai.service.AiAgentConfigService;
import top.aiolife.llm.pojo.entity.LLMKeyEntity;
import top.aiolife.llm.service.LLMKeyService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI Agent 配置服务实现。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Service
@RequiredArgsConstructor
public class AiAgentConfigServiceImpl extends ServiceImpl<AiAgentConfigMapper, AiAgentConfigEntity>
        implements AiAgentConfigService {

    private static final long SYSTEM_USER_ID = 0L;

    private static final String DEFAULT_AGENT_CODE = "life_assistant";

    private static final int DEFAULT_MAX_CONTEXT_MESSAGES = 10;

    private static final int DEFAULT_MAX_MEMORY_ITEMS = 0;

    private static final BigDecimal MIN_TEMPERATURE = BigDecimal.ZERO;

    private static final BigDecimal MAX_TEMPERATURE = BigDecimal.valueOf(2);

    private final AiAgentConfigMapper aiAgentConfigMapper;

    private final LLMKeyService llmKeyService;

    /**
     * 查询当前用户可用的 AI Agent 生效配置列表。
     *
     * @param userId 当前登录用户 id
     * @return AI Agent 生效配置列表
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public List<AiAgentConfigVO> listEffectiveConfigs(Long userId) {
        List<AiAgentConfigEntity> systemConfigs = listByUserId(SYSTEM_USER_ID);
        List<AiAgentConfigEntity> userConfigs = listByUserId(userId);
        Map<String, AiAgentConfigEntity> userConfigMap = toCodeMap(userConfigs);

        List<AiAgentConfigVO> result = new ArrayList<>();
        for (AiAgentConfigEntity systemConfig : systemConfigs) {
            if (systemConfig != null && StringUtils.hasText(systemConfig.getCode())) {
                result.add(toVO(merge(systemConfig, userConfigMap.get(systemConfig.getCode())), userConfigMap.containsKey(systemConfig.getCode())));
            }
        }
        result.sort(Comparator.comparing(AiAgentConfigVO::getCode, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    /**
     * 根据 Agent 编码查询当前用户的生效配置。
     *
     * @param userId 当前登录用户 id
     * @param code Agent 编码
     * @return AI Agent 生效配置
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public AiAgentConfigVO getEffectiveConfig(Long userId, String code) {
        String safeCode = normalizeCodeOrDefault(code);
        AiAgentConfigEntity systemConfig = getByUserIdAndCode(SYSTEM_USER_ID, safeCode);
        if (systemConfig == null) {
            throw new IllegalArgumentException("AI Agent 配置不存在: " + safeCode);
        }
        AiAgentConfigEntity userConfig = getByUserIdAndCode(userId, safeCode);
        return toVO(merge(systemConfig, userConfig), userConfig != null);
    }

    /**
     * 保存当前用户对指定 AI Agent 的覆盖配置。
     *
     * @param userId 当前登录用户 id
     * @param code Agent 编码
     * @param req 配置保存请求
     * @return 保存后的 AI Agent 生效配置
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public AiAgentConfigVO saveUserConfig(Long userId, String code, AiAgentConfigSaveReq req) {
        String safeCode = normalizeCodeOrDefault(code);
        ensureSystemConfigExists(safeCode);
        AiAgentConfigSaveReq request = req == null ? new AiAgentConfigSaveReq() : req;
        validateModelKey(userId, request.getModelKeyId());
        validateTemperature(request.getTemperature());

        AiAgentConfigEntity entity = getOrCreateUserConfig(userId, safeCode);
        entity.setName(normalizeBlank(request.getName()));
        entity.setDescription(normalizeBlank(request.getDescription()));
        entity.setModelKeyId(normalizeBlank(request.getModelKeyId()));
        entity.setSystemPrompt(normalizeBlank(request.getSystemPrompt()));
        entity.setEnabledTools(normalizeBlank(request.getEnabledTools()));
        entity.setMemoryScope(normalizeBlank(request.getMemoryScope()));
        entity.setMaxContextMessages(request.getMaxContextMessages());
        entity.setMaxMemoryItems(request.getMaxMemoryItems());
        entity.setTemperature(request.getTemperature());
        saveOrUpdateUserConfig(entity);
        return getEffectiveConfig(userId, safeCode);
    }

    /**
     * 更新当前用户对指定 AI Agent 的启用状态。
     *
     * @param userId 当前登录用户 id
     * @param code Agent 编码
     * @param enabled 是否启用
     * @return 更新后的 AI Agent 生效配置
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public AiAgentConfigVO updateStatus(Long userId, String code, Boolean enabled) {
        if (enabled == null) {
            throw new IllegalArgumentException("enabled 不能为空");
        }
        String safeCode = normalizeCodeOrDefault(code);
        ensureSystemConfigExists(safeCode);
        AiAgentConfigEntity entity = getOrCreateUserConfig(userId, safeCode);
        entity.setEnabled(enabled);
        saveOrUpdateUserConfig(entity);
        return getEffectiveConfig(userId, safeCode);
    }

    private AiAgentConfigEntity getOrCreateUserConfig(Long userId, String code) {
        AiAgentConfigEntity entity = getByUserIdAndCode(userId, code);
        if (entity != null) {
            return entity;
        }
        LocalDateTime now = LocalDateTime.now();
        entity = new AiAgentConfigEntity();
        entity.setUserId(userId);
        entity.setCode(code);
        entity.setEnabled(true);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setIsDeleted(0);
        return entity;
    }

    private void saveOrUpdateUserConfig(AiAgentConfigEntity entity) {
        entity.setUpdateTime(LocalDateTime.now());
        entity.setIsDeleted(0);
        if (entity.getId() == null) {
            aiAgentConfigMapper.insert(entity);
        } else {
            aiAgentConfigMapper.updateById(entity);
        }
    }

    private AiAgentConfigEntity merge(AiAgentConfigEntity systemConfig, AiAgentConfigEntity userConfig) {
        AiAgentConfigEntity merged = new AiAgentConfigEntity();
        AiAgentConfigEntity source = userConfig == null ? systemConfig : userConfig;
        merged.setId(source.getId());
        merged.setUserId(source.getUserId());
        merged.setCode(firstText(userConfig == null ? null : userConfig.getCode(), systemConfig.getCode(), DEFAULT_AGENT_CODE));
        merged.setName(firstText(userConfig == null ? null : userConfig.getName(), systemConfig.getName(), merged.getCode()));
        merged.setDescription(firstText(userConfig == null ? null : userConfig.getDescription(), systemConfig.getDescription(), null));
        merged.setModelKeyId(firstText(userConfig == null ? null : userConfig.getModelKeyId(), systemConfig.getModelKeyId(), null));
        merged.setSystemPrompt(firstText(userConfig == null ? null : userConfig.getSystemPrompt(), systemConfig.getSystemPrompt(), null));
        merged.setEnabledTools(firstText(userConfig == null ? null : userConfig.getEnabledTools(), systemConfig.getEnabledTools(), "[]"));
        merged.setMemoryScope(firstText(userConfig == null ? null : userConfig.getMemoryScope(), systemConfig.getMemoryScope(), "[]"));
        merged.setMaxContextMessages(firstInteger(userConfig == null ? null : userConfig.getMaxContextMessages(), systemConfig.getMaxContextMessages(), DEFAULT_MAX_CONTEXT_MESSAGES));
        merged.setMaxMemoryItems(firstInteger(userConfig == null ? null : userConfig.getMaxMemoryItems(), systemConfig.getMaxMemoryItems(), DEFAULT_MAX_MEMORY_ITEMS));
        merged.setTemperature(firstDecimal(userConfig == null ? null : userConfig.getTemperature(), systemConfig.getTemperature(), null));
        merged.setEnabled(firstBoolean(userConfig == null ? null : userConfig.getEnabled(), systemConfig.getEnabled(), true));
        merged.setCreateTime(source.getCreateTime());
        merged.setUpdateTime(source.getUpdateTime());
        merged.setIsDeleted(0);
        return merged;
    }

    private AiAgentConfigVO toVO(AiAgentConfigEntity entity, boolean userConfigured) {
        AiAgentConfigVO vo = new AiAgentConfigVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setModelKeyId(entity.getModelKeyId());
        vo.setSystemPrompt(entity.getSystemPrompt());
        vo.setEnabledTools(entity.getEnabledTools());
        vo.setMemoryScope(entity.getMemoryScope());
        vo.setMaxContextMessages(entity.getMaxContextMessages());
        vo.setMaxMemoryItems(entity.getMaxMemoryItems());
        vo.setTemperature(entity.getTemperature());
        vo.setEnabled(entity.getEnabled());
        vo.setUserConfigured(userConfigured);
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private List<AiAgentConfigEntity> listByUserId(Long userId) {
        LambdaQueryWrapper<AiAgentConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAgentConfigEntity::getUserId, userId);
        wrapper.orderByAsc(AiAgentConfigEntity::getCode);
        return aiAgentConfigMapper.selectList(wrapper);
    }

    private AiAgentConfigEntity getByUserIdAndCode(Long userId, String code) {
        LambdaQueryWrapper<AiAgentConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAgentConfigEntity::getUserId, userId);
        wrapper.eq(AiAgentConfigEntity::getCode, code);
        wrapper.last("limit 1");
        return aiAgentConfigMapper.selectOne(wrapper);
    }

    private Map<String, AiAgentConfigEntity> toCodeMap(List<AiAgentConfigEntity> configs) {
        Map<String, AiAgentConfigEntity> result = new LinkedHashMap<>();
        if (configs != null) {
            for (AiAgentConfigEntity config : configs) {
                if (config != null && StringUtils.hasText(config.getCode())) {
                    result.put(config.getCode(), config);
                }
            }
        }
        return result;
    }

    private void ensureSystemConfigExists(String code) {
        if (getByUserIdAndCode(SYSTEM_USER_ID, code) == null) {
            throw new IllegalArgumentException("AI Agent 配置不存在: " + code);
        }
    }

    private void validateModelKey(Long userId, String modelKeyId) {
        if (!StringUtils.hasText(modelKeyId)) {
            return;
        }
        List<LLMKeyEntity> keys = llmKeyService.getLLMKeyList(userId);
        boolean matched = keys != null && keys.stream().anyMatch(key -> Objects.equals(key.getId(), modelKeyId.trim()));
        if (!matched) {
            throw new IllegalArgumentException("模型配置不存在或不属于当前用户");
        }
    }

    private void validateTemperature(BigDecimal temperature) {
        if (temperature == null) {
            return;
        }
        if (temperature.compareTo(MIN_TEMPERATURE) < 0 || temperature.compareTo(MAX_TEMPERATURE) > 0) {
            throw new IllegalArgumentException("temperature 必须在 0.0 到 2.0 之间");
        }
    }

    private String normalizeCodeOrDefault(String code) {
        return StringUtils.hasText(code) ? code.trim() : DEFAULT_AGENT_CODE;
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String firstText(String first, String second, String fallback) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        if (StringUtils.hasText(second)) {
            return second.trim();
        }
        return fallback;
    }

    private Integer firstInteger(Integer first, Integer second, Integer fallback) {
        if (first != null) {
            return first;
        }
        return second == null ? fallback : second;
    }

    private BigDecimal firstDecimal(BigDecimal first, BigDecimal second, BigDecimal fallback) {
        if (first != null) {
            return first;
        }
        return second == null ? fallback : second;
    }

    private Boolean firstBoolean(Boolean first, Boolean second, Boolean fallback) {
        if (first != null) {
            return first;
        }
        return second == null ? fallback : second;
    }
}
