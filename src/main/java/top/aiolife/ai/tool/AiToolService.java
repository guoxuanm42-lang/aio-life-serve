package top.aiolife.ai.tool;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;

import java.util.Map;

/**
 * AI 工具适配服务接口。
 *
 * @author Ethan
 * @date 2026-06-28
 */
public interface AiToolService {

    /**
     * 根据 Agent 配置构建当前用户可用的 LangChain4j 工具映射。
     *
     * @param userId 当前登录用户 id
     * @param agentConfig Agent 生效配置
     * @return LangChain4j 工具定义和执行器映射
     *
     * @author Ethan
     * @date 2026-06-28
     */
    Map<ToolSpecification, ToolExecutor> buildTools(Long userId, AiAgentConfigVO agentConfig);
}
