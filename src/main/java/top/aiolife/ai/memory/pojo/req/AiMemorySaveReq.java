package top.aiolife.ai.memory.pojo.req;

import lombok.Data;

/**
 * AI 长期记忆保存请求。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
public class AiMemorySaveReq {

    /**
     * Agent 编码。
     */
    private String agentCode;

    /**
     * 记忆类型。
     */
    private String memoryType;

    /**
     * 记忆键。
     */
    private String memoryKey;

    /**
     * 记忆内容。
     */
    private String memoryValue;

    /**
     * 来源类型。
     */
    private String sourceType;

    /**
     * 来源业务 id。
     */
    private String sourceId;

    /**
     * 重要度。
     */
    private Integer importance;
}
