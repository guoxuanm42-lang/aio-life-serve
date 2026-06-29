package top.aiolife.ai.memory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.ai.memory.mapper.AiMemoryMapper;
import top.aiolife.ai.memory.pojo.entity.AiMemoryEntity;
import top.aiolife.ai.memory.pojo.req.AiMemorySaveReq;
import top.aiolife.ai.memory.pojo.vo.AiMemoryVO;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 长期记忆服务单元测试。
 *
 * @author Ethan
 * @date 2026-06-29
 */
class AiMemoryServiceImplTest {

    private static final Long USER_ID = 1001L;

    /**
     * 验证保存记忆时填充默认 Agent、类型、重要度和启用状态。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldFillDefaultsWhenSavingMemory() {
        AiMemoryMapper mapper = mock(AiMemoryMapper.class);
        AiMemoryServiceImpl service = new AiMemoryServiceImpl(mapper);
        AiMemorySaveReq req = new AiMemorySaveReq();
        req.setMemoryKey("偏好");
        req.setMemoryValue("喜欢简洁回答");
        ArgumentCaptor<AiMemoryEntity> captor = ArgumentCaptor.forClass(AiMemoryEntity.class);

        when(mapper.insert(any(AiMemoryEntity.class))).thenReturn(1);

        AiMemoryVO result = service.saveMemory(USER_ID, req);

        verify(mapper).insert(captor.capture());
        AiMemoryEntity inserted = captor.getValue();
        assertEquals("life_assistant", inserted.getAgentCode());
        assertEquals("fact", inserted.getMemoryType());
        assertEquals(50, inserted.getImportance());
        assertTrue(inserted.getEnabled());
        assertEquals("偏好", result.getMemoryKey());
    }

    /**
     * 验证更新记忆只更新当前用户拥有的数据。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldUpdateOwnedMemory() {
        AiMemoryMapper mapper = mock(AiMemoryMapper.class);
        AiMemoryServiceImpl service = new AiMemoryServiceImpl(mapper);
        AiMemoryEntity existing = buildMemory(1L, USER_ID, "life_assistant", "fact", "旧键", "旧值", true, 40);
        AiMemorySaveReq req = new AiMemorySaveReq();
        req.setAgentCode("coding_assistant");
        req.setMemoryType("preference");
        req.setMemoryKey("新键");
        req.setMemoryValue("新值");
        req.setImportance(80);
        ArgumentCaptor<AiMemoryEntity> captor = ArgumentCaptor.forClass(AiMemoryEntity.class);

        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(mapper.updateById(any(AiMemoryEntity.class))).thenReturn(1);

        AiMemoryVO result = service.updateMemory(USER_ID, 1L, req);

        verify(mapper).updateById(captor.capture());
        AiMemoryEntity updated = captor.getValue();
        assertEquals("coding_assistant", updated.getAgentCode());
        assertEquals("preference", updated.getMemoryType());
        assertEquals("新键", result.getMemoryKey());
        assertEquals(80, result.getImportance());
    }

    /**
     * 验证更新状态拒绝空 enabled，并能正确写入状态。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldUpdateMemoryStatusAndRejectNullEnabled() {
        AiMemoryMapper mapper = mock(AiMemoryMapper.class);
        AiMemoryServiceImpl service = new AiMemoryServiceImpl(mapper);
        AiMemoryEntity existing = buildMemory(1L, USER_ID, "life_assistant", "fact", "键", "值", true, 40);
        ArgumentCaptor<AiMemoryEntity> captor = ArgumentCaptor.forClass(AiMemoryEntity.class);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateStatus(USER_ID, 1L, null)
        );
        assertEquals("enabled 不能为空", exception.getMessage());

        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(mapper.updateById(any(AiMemoryEntity.class))).thenReturn(1);

        AiMemoryVO result = service.updateStatus(USER_ID, 1L, false);

        verify(mapper).updateById(captor.capture());
        assertFalse(captor.getValue().getEnabled());
        assertFalse(result.getEnabled());
    }

    /**
     * 验证最大注入条数小于等于 0 时不查询数据库。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldReturnEmptyEffectiveMemoriesWhenLimitNotPositive() {
        AiMemoryMapper mapper = mock(AiMemoryMapper.class);
        AiMemoryServiceImpl service = new AiMemoryServiceImpl(mapper);

        List<AiMemoryVO> result = service.listEffectiveMemories(USER_ID, "life_assistant", 0);

        assertTrue(result.isEmpty());
        verify(mapper, never()).selectList(any());
    }

    /**
     * 验证聊天注入记忆按 Mapper 返回结果转换为 VO。
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @Test
    void shouldListEffectiveMemories() {
        AiMemoryMapper mapper = mock(AiMemoryMapper.class);
        AiMemoryServiceImpl service = new AiMemoryServiceImpl(mapper);
        AiMemoryEntity memory = buildMemory(1L, USER_ID, "coding_assistant", "fact", "语言", "Java", true, 90);

        when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(memory));

        List<AiMemoryVO> result = service.listEffectiveMemories(USER_ID, "coding_assistant", 3);

        assertEquals(1, result.size());
        assertEquals("coding_assistant", result.getFirst().getAgentCode());
        assertEquals("语言", result.getFirst().getMemoryKey());
        assertEquals(90, result.getFirst().getImportance());
    }

    private AiMemoryEntity buildMemory(Long id,
                                       Long userId,
                                       String agentCode,
                                       String memoryType,
                                       String memoryKey,
                                       String memoryValue,
                                       Boolean enabled,
                                       Integer importance) {
        AiMemoryEntity entity = new AiMemoryEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setAgentCode(agentCode);
        entity.setMemoryType(memoryType);
        entity.setMemoryKey(memoryKey);
        entity.setMemoryValue(memoryValue);
        entity.setEnabled(enabled);
        entity.setImportance(importance);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setIsDeleted(0);
        return entity;
    }
}
