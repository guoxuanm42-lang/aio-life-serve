package top.aiolife.ai.tool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecutor;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.web.context.request.RequestAttributes;
import top.aiolife.mcp.service.IMcpToolCallService;

import java.util.List;
import java.util.Map;

/**
 * AI 调用 MCP 工具的 LangChain4j 执行器。
 *
 * @author Ethan
 * @date 2026-06-28
 */
public class AiMcpToolExecutor implements ToolExecutor {

    private final String toolName;

    private final Long userId;

    private final RequestAttributes requestAttributes;

    private final IMcpToolCallService mcpToolCallService;

    private final ObjectMapper objectMapper;

    /**
     * 创建 AI MCP 工具执行器。
     *
     * @param toolName MCP 工具名称
     * @param userId 当前登录用户 id
     * @param requestAttributes 当前请求上下文
     * @param mcpToolCallService MCP 工具调用服务
     * @param objectMapper JSON 转换器
     *
     * @author Ethan
     * @date 2026-06-28
     */
    public AiMcpToolExecutor(
            String toolName,
            Long userId,
            RequestAttributes requestAttributes,
            IMcpToolCallService mcpToolCallService,
            ObjectMapper objectMapper
    ) {
        this.toolName = toolName;
        this.userId = userId;
        this.requestAttributes = requestAttributes;
        this.mcpToolCallService = mcpToolCallService;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行 LangChain4j 工具调用，并委托现有 MCP 调用服务完成治理和日志记录。
     *
     * @param request LangChain4j 工具调用请求
     * @param memoryId LangChain4j 记忆 id
     * @return 工具执行结果文本
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @Override
    public String execute(ToolExecutionRequest request, Object memoryId) {
        Map<String, Object> arguments = parseArguments(request);
        McpSchema.CallToolResult result = mcpToolCallService.invoke(toolName, arguments, userId, requestAttributes);
        return extractText(result);
    }

    private Map<String, Object> parseArguments(ToolExecutionRequest request) {
        if (request == null || request.arguments() == null || request.arguments().isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(request.arguments(), new TypeReference<>() {
            });
        } catch (Exception exception) {
            return Map.of("rawArguments", request.arguments());
        }
    }

    private String extractText(McpSchema.CallToolResult result) {
        if (result == null || result.content() == null) {
            return "";
        }
        List<McpSchema.Content> content = result.content();
        for (McpSchema.Content item : content) {
            if (item instanceof McpSchema.TextContent textContent) {
                return textContent.text();
            }
        }
        if (result.structuredContent() != null) {
            try {
                return objectMapper.writeValueAsString(result.structuredContent());
            } catch (Exception ignored) {
                return String.valueOf(result.structuredContent());
            }
        }
        return "";
    }
}
