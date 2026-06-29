package top.aiolife.ai.memory.pojo.req;

import lombok.Data;

/**
 * AI 长期记忆状态更新请求。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Data
public class AiMemoryStatusReq {

    /**
     * 是否启用。
     */
    private Boolean enabled;
}
