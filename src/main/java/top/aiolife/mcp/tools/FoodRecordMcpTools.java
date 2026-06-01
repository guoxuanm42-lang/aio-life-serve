package top.aiolife.mcp.tools;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.core.resq.PageResp;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.pojo.req.FoodRecordIngredientToolReq;
import top.aiolife.mcp.pojo.req.FoodRecordQueryToolReq;
import top.aiolife.mcp.pojo.req.FoodRecordSaveToolReq;
import top.aiolife.mcp.pojo.req.FoodRecordStepToolReq;
import top.aiolife.mcp.pojo.vo.FoodRecordQueryToolVO;
import top.aiolife.record.pojo.req.FoodRecordIngredientSaveReq;
import top.aiolife.record.pojo.req.FoodRecordSaveReq;
import top.aiolife.record.pojo.req.FoodRecordStepSaveReq;
import top.aiolife.record.pojo.vo.FoodRecordDetailVO;
import top.aiolife.record.service.FoodRecordAiFacade;

import java.util.List;

/**
 * 美食记录 MCP 工具，提供文字结构化保存和历史查询能力。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Component
@RequiredArgsConstructor
public class FoodRecordMcpTools {

    private final FoodRecordAiFacade foodRecordAiFacade;

    /**
     * 保存一条美食记录。
     *
     * @param req MCP 美食记录保存请求，包含基础信息、时间、材料、步骤、评价和复盘
     * @return 统一返回结构，data 为保存后的美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @McpOperation(
            name = "food_record_save",
            description = "保存美食记录文字内容；支持创建或更新，创建时可通过 idempotencyKey 防止重复写入；第一版不处理图片"
    )
    public ApiResponse<FoodRecordDetailVO> save(FoodRecordSaveToolReq req) {
        FoodRecordSaveReq saveReq = toSaveReq(req);
        long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(foodRecordAiFacade.save(saveReq, userId, req == null ? null : req.getIdempotencyKey()));
    }

    /**
     * 查询美食记录历史摘要。
     *
     * @param req MCP 美食记录查询请求
     * @return 统一返回结构，data 为美食记录摘要分页
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @McpOperation(
            name = "food_record_query",
            description = "查询当前用户的美食记录历史，返回适合 Agent 阅读的文字摘要，不包含图片"
    )
    public ApiResponse<PageResp<FoodRecordQueryToolVO>> query(FoodRecordQueryToolReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(foodRecordAiFacade.query(req, userId));
    }

    private FoodRecordSaveReq toSaveReq(FoodRecordSaveToolReq req) {
        if (req == null) {
            return null;
        }
        FoodRecordSaveReq saveReq = new FoodRecordSaveReq();
        saveReq.setId(req.getId());
        saveReq.setDishName(req.getDishName());
        saveReq.setCategory(req.getCategory());
        saveReq.setMealType(req.getMealType());
        saveReq.setCookDate(req.getCookDate());
        saveReq.setStatus(req.getStatus());
        saveReq.setTags(req.getTags());
        saveReq.setDifficulty(req.getDifficulty());
        saveReq.setRating(req.getRating());
        saveReq.setSuccessLevel(req.getSuccessLevel());
        saveReq.setPrepMinutes(req.getPrepMinutes());
        saveReq.setCookMinutes(req.getCookMinutes());
        saveReq.setTotalMinutes(req.getTotalMinutes());
        saveReq.setTasteDescription(req.getTasteDescription());
        saveReq.setProblems(req.getProblems());
        saveReq.setSummary(req.getSummary());
        saveReq.setBriefSummary(req.getBriefSummary());
        saveReq.setNextImprove(req.getNextImprove());
        saveReq.setWorthRedo(req.getWorthRedo());
        saveReq.setNextTrySuggestion(req.getNextTrySuggestion());
        saveReq.setIngredients(toIngredientReqs(req.getIngredients()));
        saveReq.setSteps(toStepReqs(req.getSteps()));
        return saveReq;
    }

    private List<FoodRecordIngredientSaveReq> toIngredientReqs(List<FoodRecordIngredientToolReq> ingredients) {
        if (ingredients == null) {
            return null;
        }
        return ingredients.stream().map(item -> {
            FoodRecordIngredientSaveReq saveReq = new FoodRecordIngredientSaveReq();
            saveReq.setName(item.getName());
            saveReq.setQuantity(item.getQuantity());
            saveReq.setUnit(item.getUnit());
            saveReq.setRemark(item.getRemark());
            saveReq.setSortOrder(item.getSortOrder());
            return saveReq;
        }).toList();
    }

    private List<FoodRecordStepSaveReq> toStepReqs(List<FoodRecordStepToolReq> steps) {
        if (steps == null) {
            return null;
        }
        return steps.stream().map(item -> {
            FoodRecordStepSaveReq saveReq = new FoodRecordStepSaveReq();
            saveReq.setStepNo(item.getStepNo());
            saveReq.setTitle(item.getTitle());
            saveReq.setDescription(item.getDescription());
            saveReq.setDurationMinutes(item.getDurationMinutes());
            saveReq.setSortOrder(item.getSortOrder());
            return saveReq;
        }).toList();
    }
}
