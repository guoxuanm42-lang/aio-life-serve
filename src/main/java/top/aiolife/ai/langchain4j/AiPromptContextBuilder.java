package top.aiolife.ai.langchain4j;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.ai.memory.pojo.vo.AiMemoryVO;

import java.util.List;

/**
 * AI 系统上下文构建器。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Component
public class AiPromptContextBuilder {

    /**
     * 构建传入 LangChain4j AI Services 的系统消息。
     *
     * @param systemPrompt Agent 系统提示词
     * @param memories 可注入的长期记忆列表
     * @param legacyContext 兼容旧接口的额外上下文
     * @return 系统消息内容
     *
     * @author Ethan
     * @date 2026-06-28
     */
    public String buildSystemMessage(String systemPrompt, List<AiMemoryVO> memories, String legacyContext) {
        StringBuilder builder = new StringBuilder();
        appendSection(builder, null, systemPrompt);
        appendMemories(builder, memories);
        appendSection(builder, "当前额外上下文：", legacyContext);
        return builder.toString().trim();
    }

    private void appendMemories(StringBuilder builder, List<AiMemoryVO> memories) {
        if (memories == null || memories.isEmpty()) {
            return;
        }
        appendBlankLine(builder);
        builder.append("以下是用户长期记忆，请在相关时参考：");
        for (AiMemoryVO memory : memories) {
            if (memory != null && StringUtils.hasText(memory.getMemoryKey()) && StringUtils.hasText(memory.getMemoryValue())) {
                builder.append("\n- ")
                        .append(memory.getMemoryKey().trim())
                        .append("：")
                        .append(memory.getMemoryValue().trim());
            }
        }
    }

    private void appendSection(StringBuilder builder, String title, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        appendBlankLine(builder);
        if (StringUtils.hasText(title)) {
            builder.append(title).append("\n");
        }
        builder.append(content.trim());
    }

    private void appendBlankLine(StringBuilder builder) {
        if (!builder.isEmpty()) {
            builder.append("\n\n");
        }
    }
}
