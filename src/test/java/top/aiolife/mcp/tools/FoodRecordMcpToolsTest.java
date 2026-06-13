package top.aiolife.mcp.tools;

import org.junit.jupiter.api.Test;
import top.aiolife.mcp.pojo.req.FoodRecordIngredientToolReq;
import top.aiolife.mcp.pojo.req.FoodRecordSaveToolReq;
import top.aiolife.mcp.pojo.req.FoodRecordStepToolReq;
import top.aiolife.record.pojo.req.FoodRecordSaveReq;
import top.aiolife.record.service.FoodRecordAiFacade;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * 美食记录 MCP 工具测试，验证轻量保存请求到业务保存请求的映射。
 *
 * @author Ethan
 * @date 2026-06-10
 */
class FoodRecordMcpToolsTest {

    @Test
    void shouldMapLightweightSaveRequestAndLeaveOrderingToService() throws Exception {
        FoodRecordIngredientToolReq ingredient = new FoodRecordIngredientToolReq();
        ingredient.setName("番茄");
        ingredient.setQuantity("2");
        ingredient.setUnit("个");
        ingredient.setRemark("切块");

        FoodRecordStepToolReq step = new FoodRecordStepToolReq();
        step.setTitle("备菜");
        step.setDescription("番茄切块，鸡蛋打散");
        step.setDurationMinutes(5);

        FoodRecordSaveToolReq req = new FoodRecordSaveToolReq();
        req.setId(10L);
        req.setDishName("番茄炒蛋");
        req.setCookDate(LocalDate.of(2026, 6, 10));
        req.setStatus("draft");
        req.setIngredients(List.of(ingredient));
        req.setSteps(List.of(step));

        FoodRecordSaveReq saveReq = invokeToSaveReq(req);

        assertEquals(10L, saveReq.getId());
        assertEquals("番茄炒蛋", saveReq.getDishName());
        assertEquals(LocalDate.of(2026, 6, 10), saveReq.getCookDate());
        assertEquals("draft", saveReq.getStatus());
        assertEquals("番茄", saveReq.getIngredients().getFirst().getName());
        assertNull(saveReq.getIngredients().getFirst().getSortOrder());
        assertEquals("备菜", saveReq.getSteps().getFirst().getTitle());
        assertEquals(5, saveReq.getSteps().getFirst().getDurationMinutes());
        assertNull(saveReq.getSteps().getFirst().getStepNo());
        assertNull(saveReq.getSteps().getFirst().getSortOrder());
    }

    @Test
    void shouldMapMinimumDishNameForDraftCreation() throws Exception {
        FoodRecordSaveToolReq req = new FoodRecordSaveToolReq();
        req.setDishName("番茄炒蛋");

        FoodRecordSaveReq saveReq = invokeToSaveReq(req);

        assertEquals("番茄炒蛋", saveReq.getDishName());
        assertNull(saveReq.getStatus());
        assertNull(saveReq.getIngredients());
        assertNull(saveReq.getSteps());
    }

    private FoodRecordSaveReq invokeToSaveReq(FoodRecordSaveToolReq req) throws Exception {
        FoodRecordMcpTools tools = new FoodRecordMcpTools(mock(FoodRecordAiFacade.class));
        Method method = FoodRecordMcpTools.class.getDeclaredMethod("toSaveReq", FoodRecordSaveToolReq.class);
        method.setAccessible(true);
        return (FoodRecordSaveReq) method.invoke(tools, req);
    }
}
