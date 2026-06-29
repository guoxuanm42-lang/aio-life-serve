package top.aiolife.ai.pojo.req;

import lombok.Data;

/**
 * AI Agent 状态更新请求对象。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
public class AiAgentStatusReq {

    /**
     * 是否启用当前 Agent。
     */
    private Boolean enabled;
}
