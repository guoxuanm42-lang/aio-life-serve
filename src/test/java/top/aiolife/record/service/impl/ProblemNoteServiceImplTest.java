package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import top.aiolife.record.mapper.IProblemCategoryMapper;
import top.aiolife.record.mapper.IProblemNoteMapper;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;
import top.aiolife.record.pojo.req.ProblemNoteSaveReq;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 题目记录服务测试，验证题目写入必填字段和可选代码字段。
 *
 * @author Ethan
 * @date 2026-06-23
 */
class ProblemNoteServiceImplTest {

    @Test
    void shouldRejectBlankTitleWhenCreatingProblemNote() {
        ProblemNoteServiceImpl service = newService(mock(IProblemNoteMapper.class));
        ProblemNoteSaveReq req = validReq();
        req.setTitle(" ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(req, 1L));

        assertEquals("题目标题不能为空", exception.getMessage());
    }

    @Test
    void shouldRejectBlankProblemContentWhenCreatingProblemNote() {
        ProblemNoteServiceImpl service = newService(mock(IProblemNoteMapper.class));
        ProblemNoteSaveReq req = validReq();
        req.setProblemContent(" ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(req, 1L));

        assertEquals("题目内容不能为空", exception.getMessage());
    }

    @Test
    void shouldAllowBlankSolutionCodeWhenCreatingProblemNote() {
        IProblemNoteMapper problemNoteMapper = mock(IProblemNoteMapper.class);
        ProblemNoteServiceImpl service = newService(problemNoteMapper);
        ProblemNoteSaveReq req = validReq();
        req.setSolutionCode(" ");
        ProblemNoteEntity saved = new ProblemNoteEntity();
        saved.setId(100L);
        saved.setUserId(1L);
        saved.setTitle(req.getTitle());
        saved.setProblemContent(req.getProblemContent());
        when(problemNoteMapper.insert(any(ProblemNoteEntity.class))).thenAnswer(invocation -> {
            ProblemNoteEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });
        when(problemNoteMapper.selectOne(any(Wrapper.class))).thenReturn(saved);

        assertDoesNotThrow(() -> service.create(req, 1L));
    }

    private ProblemNoteServiceImpl newService(IProblemNoteMapper problemNoteMapper) {
        return new ProblemNoteServiceImpl(problemNoteMapper, mock(IProblemCategoryMapper.class));
    }

    private ProblemNoteSaveReq validReq() {
        ProblemNoteSaveReq req = new ProblemNoteSaveReq();
        req.setTitle("两数之和");
        req.setProblemContent("给定数组和目标值，返回两数下标。");
        req.setSolutionCode("class Solution {}");
        req.setStatus("draft");
        return req;
    }
}
