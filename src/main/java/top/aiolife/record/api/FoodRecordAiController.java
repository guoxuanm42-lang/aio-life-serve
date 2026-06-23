package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.llm.service.LLMKeyService;
import top.aiolife.llm.service.LLMService;
import top.aiolife.record.pojo.req.FoodRecipeGenerateReq;
import top.aiolife.record.pojo.req.FoodRecordSaveReq;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * 美食记录 AI 控制器，提供食谱草稿生成能力且不直接写入美食记录。
 *
 * @author Ethan
 * @date 2026-06-23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/food-record/ai")
public class FoodRecordAiController {

    private static final String DEFAULT_STATUS = "draft";

    private final LLMService llmService;
    private final LLMKeyService llmKeyService;
    private final ObjectMapper objectMapper;

    /**
     * 生成美食记录草稿接口。
     *
     * <p>用途：前端提交自然语言食谱需求，后端调用当前用户默认大模型生成结构化草稿，并返回给前端预览。</p>
     *
     * @param req AI 食谱生成请求，prompt 为用户输入的自然语言需求
     * @return 统一返回结构，data 为可填入新增美食记录表单的草稿内容
     *
     * @author Ethan
     * @date 2026-06-23
     */
    @PostMapping("/generate-recipe")
    public ApiResponse<FoodRecordSaveReq> generateRecipe(@RequestBody FoodRecipeGenerateReq req) {
        if (req == null || !StringUtils.hasText(req.getPrompt())) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, "请输入食谱生成需求");
        }

        long userId = StpUtil.getLoginIdAsLong();
        var llmKey = llmKeyService.getDefaultLLMKey(userId);
        if (llmKey == null) {
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "请先配置大模型 API Key");
        }

        try {
            String response = llmService.generateResponse(
                    llmKey.getApiKey(),
                    llmKey.getBaseUrl(),
                    llmKey.getModelName(),
                    buildPrompt(req),
                    null
            );
            FoodRecordSaveReq draft = parseDraft(response);
            normalizeDraft(draft);
            if (!StringUtils.hasText(draft.getDishName())) {
                return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "生成结果缺少菜名，请调整描述后重试");
            }
            return ApiResponse.success(draft);
        } catch (JsonProcessingException exception) {
            log.warn("AI 食谱草稿 JSON 解析失败", exception);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "AI 返回格式异常，请重试");
        } catch (Exception exception) {
            log.error("AI 食谱草稿生成失败", exception);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, exception.getMessage());
        }
    }

    private String buildPrompt(String userPrompt) {
        return """
                你是 AIO-LIFE 美食模块的食谱草稿生成助手。
                请根据用户需求生成一份适合写入美食记录的结构化食谱草稿。
                只返回一个 JSON 对象，不要返回 Markdown，不要使用 ``` 包裹，不要解释。
                不要保存数据库，不要调用任何外部工具。

                JSON 字段要求：
                {
                  "dishName": "菜名，必填",
                  "category": "分类，如 家常菜/主食/汤羹/甜品/饮品",
                  "mealType": "餐次，如 早餐/午餐/晚餐/夜宵/加餐，可为空",
                  "cookDate": "yyyy-MM-dd，可省略",
                  "status": "draft",
                  "tags": "逗号分隔标签",
                  "difficulty": "简单/中等/稍复杂",
                  "prepMinutes": 备菜分钟数,
                  "cookMinutes": 烹饪分钟数,
                  "totalMinutes": 总分钟数,
                  "tasteDescription": "口味描述",
                  "summary": "整体说明",
                  "briefSummary": "一句话总结",
                  "nextImprove": "注意事项或下次改进",
                  "nextTrySuggestion": "下次尝试建议",
                  "worthRedo": true,
                  "ingredients": [
                    {"name": "食材名", "quantity": "数量", "unit": "单位", "remark": "备注"}
                  ],
                  "steps": [
                    {"stepNo": 1, "title": "步骤标题", "description": "步骤说明", "durationMinutes": 分钟数}
                  ]
                }
                用户信息不完整时，请自行合理补全，不要反问。
                用户需求：
                """ + userPrompt;
    }

    private String buildPrompt(FoodRecipeGenerateReq req) throws JsonProcessingException {
        String prompt = buildPrompt(req.getPrompt().trim());
        if (req.getCurrentDraft() == null) {
            return prompt;
        }
        String instruction = StringUtils.hasText(req.getInstruction())
                ? req.getInstruction().trim()
                : "请基于原始需求和当前草稿重新生成一份不同的完整食谱草稿";
        return prompt + """

                当前已有草稿 JSON：
                """ + objectMapper.writeValueAsString(req.getCurrentDraft()) + """

                本次改稿要求：
                """ + instruction + """

                请基于当前草稿和改稿要求，返回一份新的完整 JSON 草稿。需要保留合理内容，但必须让结果体现本次改稿要求。
                """;
    }

    private FoodRecordSaveReq parseDraft(String response) throws JsonProcessingException {
        String json = normalizeJson(response);
        return objectMapper.readerFor(FoodRecordSaveReq.class)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(json);
    }

    private String normalizeJson(String response) throws JsonProcessingException {
        if (!StringUtils.hasText(response)) {
            throw new JsonProcessingException("empty AI response") {
            };
        }
        String text = response.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json|JSON)?\\s*", "");
            text = text.replaceFirst("\\s*```$", "");
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }
        return text.trim();
    }

    private void normalizeDraft(FoodRecordSaveReq draft) {
        draft.setId(null);
        draft.setStatus(DEFAULT_STATUS);
        if (draft.getCookDate() == null) {
            draft.setCookDate(LocalDate.now());
        }
        if (draft.getIngredients() == null) {
            draft.setIngredients(new ArrayList<>());
        }
        if (draft.getSteps() == null) {
            draft.setSteps(new ArrayList<>());
        }
        if (draft.getTotalMinutes() == null) {
            Integer prep = draft.getPrepMinutes();
            Integer cook = draft.getCookMinutes();
            if (prep != null || cook != null) {
                draft.setTotalMinutes((prep == null ? 0 : prep) + (cook == null ? 0 : cook));
            }
        }
    }
}
