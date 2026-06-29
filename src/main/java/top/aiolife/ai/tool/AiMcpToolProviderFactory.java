package top.aiolife.ai.tool;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI MCP 工具提供器工厂。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Component
public class AiMcpToolProviderFactory {

    /**
     * 根据工具映射创建 LangChain4j 工具提供器。
     *
     * @param tools 工具定义和执行器映射
     * @return LangChain4j 工具提供器
     *
     * @author Ethan
     * @date 2026-06-28
     */
    public ToolProvider createProvider(Map<ToolSpecification, ToolExecutor> tools) {
        Map<ToolSpecification, ToolExecutor> safeTools = tools == null ? Map.of() : tools;
        return request -> new ToolProviderResult(safeTools);
    }
}
