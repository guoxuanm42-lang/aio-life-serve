package top.aiolife.ai.activity.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.pojo.summary.AiActivitySummaryContext;

/**
 * 活动总结上下文编解码器，负责持久化和恢复生成报告时使用的结构化统计快照。
 *
 * @author Ethan
 * @date 2026-08-15
 */
@Component
@RequiredArgsConstructor
public class AiActivitySummaryContextCodec {

    private final ObjectMapper objectMapper;

    /**
     * 将活动总结上下文序列化为数据库快照。
     *
     * @param context 活动总结结构化上下文
     * @return 可持久化的 JSON 字符串
     * @throws IllegalStateException 上下文无法序列化时抛出
     *
     * @author Ethan
     * @date 2026-08-15
     */
    public String serialize(AiActivitySummaryContext context) {
        if (context == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("活动总结上下文序列化失败", exception);
        }
    }

    /**
     * 从数据库快照恢复活动总结上下文。
     *
     * @param contextJson 活动总结上下文 JSON；为空表示旧报告没有快照
     * @return 恢复后的上下文，快照为空时返回 null
     * @throws IllegalStateException 快照格式无效时抛出
     *
     * @author Ethan
     * @date 2026-08-15
     */
    public AiActivitySummaryContext deserialize(String contextJson) {
        if (!StringUtils.hasText(contextJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(contextJson, AiActivitySummaryContext.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("活动总结上下文快照格式无效", exception);
        }
    }
}
