package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import top.aiolife.ai.activity.mapper.AiActivitySummaryGenerationMapper;
import top.aiolife.ai.activity.pojo.entity.AiActivitySummaryGenerationEntity;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;
import top.aiolife.ai.activity.support.AiActivitySummaryContextCodec;
import top.aiolife.llm.pojo.entity.ChatMessageEntity;
import top.aiolife.llm.pojo.resp.ChatMessageResp;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 活动总结历史装配服务测试，验证批量关联范围和旧消息兼容行为。
 *
 * @author Ethan
 * @date 2026-08-15
 */
class AiActivitySummaryHistoryServiceImplTest {

    @Test
    void shouldAttachSnapshotOnlyToActivitySummaryAssistantMessage() {
        AiActivitySummaryGenerationMapper mapper = mock(AiActivitySummaryGenerationMapper.class);
        AiActivitySummaryContextCodec codec = mock(AiActivitySummaryContextCodec.class);
        AiActivitySummaryContext context = new AiActivitySummaryContext();
        context.setPeriod("week");
        AiActivitySummaryGenerationEntity report = new AiActivitySummaryGenerationEntity();
        report.setAssistantMessageId(2L);
        report.setContextJson("{}");
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(report));
        when(codec.deserialize("{}")).thenReturn(context);
        AiActivitySummaryHistoryServiceImpl service = new AiActivitySummaryHistoryServiceImpl(mapper, codec);

        List<ChatMessageResp> result = service.assemble(10L, List.of(
                message(1L, "user", "activity_summary"),
                message(2L, "assistant", "activity_summary"),
                message(3L, "assistant", null)));

        assertEquals(3, result.size());
        assertNull(result.get(0).getActivitySummary());
        assertEquals(context, result.get(1).getActivitySummary());
        assertNull(result.get(2).getActivitySummary());
        verify(mapper).selectList(any(Wrapper.class));
    }

    @Test
    void shouldKeepLegacyReportWithoutSnapshotRenderable() {
        AiActivitySummaryGenerationMapper mapper = mock(AiActivitySummaryGenerationMapper.class);
        AiActivitySummaryContextCodec codec = mock(AiActivitySummaryContextCodec.class);
        AiActivitySummaryGenerationEntity report = new AiActivitySummaryGenerationEntity();
        report.setAssistantMessageId(2L);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(report));
        AiActivitySummaryHistoryServiceImpl service = new AiActivitySummaryHistoryServiceImpl(mapper, codec);

        List<ChatMessageResp> result = service.assemble(
                10L, List.of(message(2L, "assistant", "activity_summary")));

        assertNull(result.get(0).getActivitySummary());
    }

    private ChatMessageEntity message(Long id, String role, String sourceType) {
        ChatMessageEntity message = new ChatMessageEntity();
        message.setId(id);
        message.setUserId(10L);
        message.setRole(role);
        message.setSourceType(sourceType);
        message.setContent("内容");
        return message;
    }
}
