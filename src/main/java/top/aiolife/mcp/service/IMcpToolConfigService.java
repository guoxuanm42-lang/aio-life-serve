package top.aiolife.mcp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.mcp.definition.McpToolDefinition;
import top.aiolife.mcp.pojo.entity.McpToolConfigEntity;
import top.aiolife.mcp.pojo.req.McpToolConfigSaveReq;
import top.aiolife.mcp.pojo.vo.McpToolVO;

import java.util.Collection;
import java.util.List;

/**
 * MCP tool configuration service.
 *
 * <p>Combines runtime tool definitions with global operation configuration and manages enablement.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
public interface IMcpToolConfigService extends IService<McpToolConfigEntity> {

    /**
     * Lists MCP tools merged from runtime registry and database configuration.
     *
     * @param definitions runtime MCP tool definitions discovered by registry
     * @return merged MCP tool view objects ordered by sort order and tool name
     *
     * @author Ethan
     * @date 2026-06-03
     */
    List<McpToolVO> listMergedTools(Collection<McpToolDefinition> definitions);

    /**
     * Saves editable configuration for one MCP tool.
     *
     * @param toolName tool name from path variable
     * @param req configuration request body
     * @return saved configuration entity
     *
     * @author Ethan
     * @date 2026-06-03
     */
    McpToolConfigEntity saveConfig(String toolName, McpToolConfigSaveReq req);

    /**
     * Updates enablement status for one MCP tool.
     *
     * @param toolName tool name from path variable
     * @param enabled whether the tool should be enabled
     * @return saved configuration entity
     *
     * @author Ethan
     * @date 2026-06-03
     */
    McpToolConfigEntity updateStatus(String toolName, Boolean enabled);

    /**
     * Checks whether a tool is enabled by operation configuration.
     *
     * @param toolName runtime tool name
     * @return true when no config exists or config enabled is not false
     *
     * @author Ethan
     * @date 2026-06-03
     */
    boolean isToolEnabled(String toolName);
}
