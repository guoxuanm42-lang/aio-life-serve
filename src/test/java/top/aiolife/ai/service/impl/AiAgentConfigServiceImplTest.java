package top.aiolife.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.ai.mapper.AiAgentConfigMapper;
import top.aiolife.ai.pojo.entity.AiAgentConfigEntity;
import top.aiolife.ai.pojo.req.AiAgentConfigSaveReq;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.llm.pojo.entity.LLMKeyEntity;
import top.aiolife.llm.service.LLMKeyService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI Agent 配置服务单元测试。
 *
 * @author Ethan
 * @date 2026-06-29
 */
class AiAgentConfigServiceImplTest {

    private static final Long USER_ID = 1001L;

    /**
     * 验证无用户覆盖配置时返回系统默认配置。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldReturnSystemConfigWhenUserOverrideMissing() {
        AiAgentConfigMapper mapper = mock(AiAgentConfigMapper.class);
        LLMKeyService llmKeyService = mock(LLMKeyService.class);
        AiAgentConfigServiceImpl service = new AiAgentConfigServiceImpl(mapper, llmKeyService);
        AiAgentConfigEntity system = buildConfig(1L, 0L, "life_assistant", "生活总助理", "系统提示词", true);

        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(system, null);

        AiAgentConfigVO result = service.getEffectiveConfig(USER_ID, null);

        assertEquals("life_assistant", result.getCode());
        assertEquals("生活总助理", result.getName());
        assertEquals("系统提示词", result.getSystemPrompt());
        assertFalse(result.getUserConfigured());
    }

    /**
     * 验证用户覆盖配置优先于系统默认配置。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldPreferUserOverrideConfig() {
        AiAgentConfigMapper mapper = mock(AiAgentConfigMapper.class);
        LLMKeyService llmKeyService = mock(LLMKeyService.class);
        AiAgentConfigServiceImpl service = new AiAgentConfigServiceImpl(mapper, llmKeyService);
        AiAgentConfigEntity system = buildConfig(1L, 0L, "life_assistant", "生活总助理", "系统提示词", true);
        AiAgentConfigEntity user = buildConfig(2L, USER_ID, "life_assistant", "我的助理", "用户提示词", false);
        user.setModelKeyId("key-1");
        user.setMaxContextMessages(20);

        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(system, user);

        AiAgentConfigVO result = service.getEffectiveConfig(USER_ID, "life_assistant");

        assertEquals(USER_ID, result.getUserId());
        assertEquals("我的助理", result.getName());
        assertEquals("用户提示词", result.getSystemPrompt());
        assertEquals("key-1", result.getModelKeyId());
        assertEquals(20, result.getMaxContextMessages());
        assertFalse(result.getEnabled());
        assertTrue(result.getUserConfigured());
    }

    /**
     * 验证更新状态时会创建用户维度覆盖配置。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldCreateUserConfigWhenUpdatingStatus() {
        AiAgentConfigMapper mapper = mock(AiAgentConfigMapper.class);
        LLMKeyService llmKeyService = mock(LLMKeyService.class);
        AiAgentConfigServiceImpl service = new AiAgentConfigServiceImpl(mapper, llmKeyService);
        AiAgentConfigEntity system = buildConfig(1L, 0L, "life_assistant", "生活总助理", "系统提示词", true);
        AiAgentConfigEntity userDisabled = buildConfig(2L, USER_ID, "life_assistant", null, null, false);
        ArgumentCaptor<AiAgentConfigEntity> captor = ArgumentCaptor.forClass(AiAgentConfigEntity.class);

        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(system, null, system, userDisabled);
        when(mapper.insert(any(AiAgentConfigEntity.class))).thenReturn(1);

        AiAgentConfigVO result = service.updateStatus(USER_ID, "life_assistant", false);

        verify(mapper).insert(captor.capture());
        AiAgentConfigEntity inserted = captor.getValue();
        assertEquals(USER_ID, inserted.getUserId());
        assertEquals("life_assistant", inserted.getCode());
        assertFalse(inserted.getEnabled());
        assertEquals(0, inserted.getIsDeleted());
        assertFalse(result.getEnabled());
    }

    /**
     * 验证保存配置时模型 Key 必须属于当前用户。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldRejectModelKeyNotOwnedByUser() {
        AiAgentConfigMapper mapper = mock(AiAgentConfigMapper.class);
        LLMKeyService llmKeyService = mock(LLMKeyService.class);
        AiAgentConfigServiceImpl service = new AiAgentConfigServiceImpl(mapper, llmKeyService);
        AiAgentConfigSaveReq req = new AiAgentConfigSaveReq();
        req.setModelKeyId("missing-key");

        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildConfig(1L, 0L, "life_assistant", "生活总助理", "系统提示词", true));
        when(llmKeyService.getLLMKeyList(USER_ID)).thenReturn(List.of(buildLlmKey("owned-key")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.saveUserConfig(USER_ID, "life_assistant", req)
        );

        assertEquals("模型配置不存在或不属于当前用户", exception.getMessage());
    }

    /**
     * 验证温度参数必须在模型允许范围内。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldRejectTemperatureOutsideRange() {
        AiAgentConfigMapper mapper = mock(AiAgentConfigMapper.class);
        LLMKeyService llmKeyService = mock(LLMKeyService.class);
        AiAgentConfigServiceImpl service = new AiAgentConfigServiceImpl(mapper, llmKeyService);
        AiAgentConfigSaveReq req = new AiAgentConfigSaveReq();
        req.setTemperature(BigDecimal.valueOf(2.1));

        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildConfig(1L, 0L, "life_assistant", "生活总助理", "系统提示词", true));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.saveUserConfig(USER_ID, "life_assistant", req)
        );

        assertEquals("temperature 必须在 0.0 到 2.0 之间", exception.getMessage());
    }

    private AiAgentConfigEntity buildConfig(Long id, Long userId, String code, String name, String systemPrompt, Boolean enabled) {
        AiAgentConfigEntity entity = new AiAgentConfigEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setCode(code);
        entity.setName(name);
        entity.setDescription("description");
        entity.setSystemPrompt(systemPrompt);
        entity.setEnabledTools("[]");
        entity.setMemoryScope("[]");
        entity.setMaxContextMessages(10);
        entity.setMaxMemoryItems(0);
        entity.setEnabled(enabled);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setIsDeleted(0);
        return entity;
    }

    private LLMKeyEntity buildLlmKey(String id) {
        LLMKeyEntity entity = new LLMKeyEntity();
        entity.setId(id);
        entity.setUserId(USER_ID);
        return entity;
    }
}
