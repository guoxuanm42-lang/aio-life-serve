package top.aiolife.llm.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import top.aiolife.llm.mapper.ChatMessageMapper;
import top.aiolife.llm.pojo.entity.ChatMessageEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 聊天消息服务测试，验证历史消息在创建时间相同时使用消息 ID 保持稳定顺序。
 *
 * @author Ethan
 * @date 2026-08-15
 */
class ChatMessageServiceImplTest {

    @BeforeAll
    static void initializeTableMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), ChatMessageEntity.class);
    }

    @Test
    void shouldOrderUserMessagesByCreateTimeAndId() {
        Fixture fixture = fixture();

        fixture.service.listByUserId(1L);

        assertStableOrder(captureWrapper(fixture.mapper));
    }

    @Test
    void shouldOrderConversationMessagesByCreateTimeAndId() {
        Fixture fixture = fixture();

        fixture.service.listByconversationId(1L, 2L);

        assertStableOrder(captureWrapper(fixture.mapper));
    }

    private Fixture fixture() {
        ChatMessageMapper mapper = mock(ChatMessageMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        ChatMessageServiceImpl service = new ChatMessageServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        return new Fixture(service, mapper);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Wrapper<ChatMessageEntity> captureWrapper(ChatMessageMapper mapper) {
        ArgumentCaptor<Wrapper<ChatMessageEntity>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectList(captor.capture());
        return captor.getValue();
    }

    private void assertStableOrder(Wrapper<ChatMessageEntity> wrapper) {
        String sqlSegment = wrapper.getSqlSegment().toLowerCase();
        int orderIndex = sqlSegment.indexOf("order by");
        int createTimeIndex = sqlSegment.indexOf("create_time", orderIndex);
        int idIndex = sqlSegment.indexOf("id", createTimeIndex + "create_time".length());

        assertTrue(orderIndex >= 0, "查询必须包含 ORDER BY");
        assertTrue(createTimeIndex > orderIndex, "创建时间必须是第一排序字段");
        assertTrue(idIndex > createTimeIndex, "消息 ID 必须是第二排序字段");
    }

    private record Fixture(ChatMessageServiceImpl service, ChatMessageMapper mapper) {
    }
}
