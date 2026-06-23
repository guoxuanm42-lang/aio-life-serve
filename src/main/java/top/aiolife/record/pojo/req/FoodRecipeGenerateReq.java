package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * AI 食谱草稿生成请求，承载用户在美食模块输入的自然语言需求。
 *
 * @author Ethan
 * @date 2026-06-23
 */
@Data
public class FoodRecipeGenerateReq {

    /**
     * 用户输入的食谱生成需求。
     */
    private String prompt;

    /**
     * 当前正在预览的食谱草稿，改稿时作为 AI 参考上下文。
     */
    private FoodRecordSaveReq currentDraft;

    /**
     * 用户本次改稿指令，例如更简单、更清淡或换一道菜。
     */
    private String instruction;
}
