package top.aiolife.mcp.tools;

import org.junit.jupiter.api.Test;
import top.aiolife.mcp.pojo.req.ProblemNoteSaveToolReq;
import top.aiolife.record.pojo.req.ProblemNoteSaveReq;
import top.aiolife.record.service.ProblemNoteAiFacade;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * 题目记录 MCP 工具测试，验证新增请求到业务保存请求的字段映射。
 *
 * @author Ethan
 * @date 2026-06-24
 */
class ProblemNoteMcpToolsTest {

    @Test
    void shouldMapSaveToolRequestToProblemNoteSaveRequest() throws Exception {
        ProblemNoteSaveToolReq req = new ProblemNoteSaveToolReq();
        req.setIdempotencyKey("problem-key-1");
        req.setCategoryId(10L);
        req.setTitle("两数之和");
        req.setProblemContent("给定数组和目标值，返回两数下标。");
        req.setSolutionCode("class Solution {}");
        req.setPseudoCode("for each num, find target - num");
        req.setIdeaNote("使用哈希表记录已遍历数字。");
        req.setDifficulty("easy");
        req.setTags("数组,哈希表");
        req.setStatus("draft");

        ProblemNoteSaveReq saveReq = invokeToSaveReq(req);

        assertEquals(10L, saveReq.getCategoryId());
        assertEquals("两数之和", saveReq.getTitle());
        assertEquals("给定数组和目标值，返回两数下标。", saveReq.getProblemContent());
        assertEquals("class Solution {}", saveReq.getSolutionCode());
        assertEquals("for each num, find target - num", saveReq.getPseudoCode());
        assertEquals("使用哈希表记录已遍历数字。", saveReq.getIdeaNote());
        assertEquals("easy", saveReq.getDifficulty());
        assertEquals("数组,哈希表", saveReq.getTags());
        assertEquals("draft", saveReq.getStatus());
        assertNull(saveReq.getId());
    }

    private ProblemNoteSaveReq invokeToSaveReq(ProblemNoteSaveToolReq req) throws Exception {
        ProblemNoteMcpTools tools = new ProblemNoteMcpTools(mock(ProblemNoteAiFacade.class));
        Method method = ProblemNoteMcpTools.class.getDeclaredMethod("toSaveReq", ProblemNoteSaveToolReq.class);
        method.setAccessible(true);
        return (ProblemNoteSaveReq) method.invoke(tools, req);
    }
}
