package top.aiolife.record.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.core.resq.PageResp;
import top.aiolife.mcp.pojo.req.ProblemNoteQueryToolReq;
import top.aiolife.mcp.pojo.vo.ProblemNoteQueryToolVO;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;
import top.aiolife.record.pojo.req.ProblemNoteQueryReq;
import top.aiolife.record.pojo.req.ProblemNoteSaveReq;
import top.aiolife.record.util.RedisUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 题目记录 AI 门面测试，验证 MCP 查询映射和新增幂等控制。
 *
 * @author Ethan
 * @date 2026-06-23
 */
class ProblemNoteAiFacadeTest {

    @Test
    void shouldMapQueryToolRequestAndReturnToolVO() {
        IProblemNoteService problemNoteService = mock(IProblemNoteService.class);
        ProblemNoteAiFacade facade = new ProblemNoteAiFacade(problemNoteService, mock(RedisUtil.class));
        ProblemNoteEntity entity = entity(100L);
        when(problemNoteService.query(any(), eq(1L))).thenReturn(PageResp.of(List.of(entity), 1L));

        ProblemNoteQueryToolReq req = new ProblemNoteQueryToolReq();
        req.setPage(2);
        req.setPageSize(20);
        req.setKeyword("哈希");
        req.setDifficulty("easy");
        req.setStatus("draft");
        req.setTags("数组");
        req.setCategoryId(10L);
        req.setUncategorized(false);

        PageResp<ProblemNoteQueryToolVO> result = facade.query(req, 1L);

        ArgumentCaptor<ProblemNoteQueryReq> captor = ArgumentCaptor.forClass(ProblemNoteQueryReq.class);
        verify(problemNoteService).query(captor.capture(), eq(1L));
        ProblemNoteQueryReq queryReq = captor.getValue();
        assertEquals(2, queryReq.getPage());
        assertEquals(20, queryReq.getPageSize());
        assertEquals("哈希", queryReq.getKeyword());
        assertEquals("easy", queryReq.getDifficulty());
        assertEquals("draft", queryReq.getStatus());
        assertEquals("数组", queryReq.getTags());
        assertEquals(10L, queryReq.getCategoryId());
        assertEquals(false, queryReq.getUncategorized());
        assertEquals(1L, result.getTotal());
        assertEquals(100L, result.getItems().getFirst().getId());
        assertEquals("两数之和", result.getItems().getFirst().getTitle());
    }

    @Test
    void shouldReturnExistingProblemWhenIdempotencyKeyWasUsed() {
        IProblemNoteService problemNoteService = mock(IProblemNoteService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ProblemNoteAiFacade facade = new ProblemNoteAiFacade(problemNoteService, redisUtil);
        ProblemNoteEntity existing = entity(100L);
        when(redisUtil.get("mcp:idemp:problem_note_save:1:key-1")).thenReturn("100");
        when(problemNoteService.detail(100L, 1L)).thenReturn(existing);

        ProblemNoteEntity result = facade.create(new ProblemNoteSaveReq(), 1L, "key-1");

        assertSame(existing, result);
        verify(problemNoteService, never()).create(any(), any());
    }

    @Test
    void shouldStoreCreatedProblemIdForIdempotencyKey() {
        IProblemNoteService problemNoteService = mock(IProblemNoteService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ProblemNoteAiFacade facade = new ProblemNoteAiFacade(problemNoteService, redisUtil);
        ProblemNoteEntity created = entity(100L);
        when(redisUtil.setIfAbsent(eq("mcp:idemp:problem_note_save:1:key-1"), eq("LOCK"), anyLong(), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(problemNoteService.create(any(), eq(1L))).thenReturn(created);

        ProblemNoteEntity result = facade.create(new ProblemNoteSaveReq(), 1L, "key-1");

        assertSame(created, result);
        verify(redisUtil).set("mcp:idemp:problem_note_save:1:key-1", "100", TimeUnit.HOURS.toSeconds(24), TimeUnit.SECONDS);
    }

    @Test
    void shouldCreateDirectlyWhenIdempotencyKeyIsBlank() {
        IProblemNoteService problemNoteService = mock(IProblemNoteService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ProblemNoteAiFacade facade = new ProblemNoteAiFacade(problemNoteService, redisUtil);
        ProblemNoteEntity created = entity(100L);
        when(problemNoteService.create(any(), eq(1L))).thenReturn(created);

        ProblemNoteEntity result = facade.create(new ProblemNoteSaveReq(), 1L, " ");

        assertSame(created, result);
        verify(redisUtil, never()).setIfAbsent(any(), any(), anyLong(), any());
    }

    private ProblemNoteEntity entity(Long id) {
        ProblemNoteEntity entity = new ProblemNoteEntity();
        entity.setId(id);
        entity.setCategoryId(10L);
        entity.setTitle("两数之和");
        entity.setProblemContent("给定数组和目标值，返回两数下标。");
        entity.setSolutionCode("class Solution {}");
        entity.setIdeaNote("使用哈希表记录已遍历数字。");
        entity.setDifficulty("easy");
        entity.setTags("数组,哈希表");
        entity.setStatus("draft");
        entity.setCreateTime(LocalDateTime.of(2026, 6, 23, 10, 0));
        entity.setUpdateTime(LocalDateTime.of(2026, 6, 23, 10, 30));
        return entity;
    }
}
