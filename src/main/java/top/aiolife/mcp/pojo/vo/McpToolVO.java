package top.aiolife.mcp.pojo.vo;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.Data;

/**
 * MCP tool management view object.
 *
 * <p>Returns runtime schema, operation configuration, and display metadata to the management page.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Data
public class McpToolVO {

    /**
     * Runtime tool name.
     */
    private String name;

    /**
     * Display name shown in the management page.
     */
    private String displayName;

    /**
     * Tool description shown in the management page.
     */
    private String description;

    /**
     * Input argument schema.
     */
    private McpSchema.JsonSchema inputSchema;

    /**
     * Parameter count derived from input schema properties.
     */
    private Integer paramCount;

    /**
     * Whether the runtime tool requires login.
     */
    private Boolean authRequired;

    /**
     * Whether the tool is enabled by operation configuration.
     */
    private Boolean enabled;

    /**
     * Whether the tool may write or modify business data.
     */
    private Boolean writeOperation;

    /**
     * Tool group name.
     */
    private String groupName;

    /**
     * Whether the tool already has database configuration.
     */
    private Boolean configured;

    /**
     * Whether the tool is registered in the current runtime.
     */
    private Boolean runtimeRegistered;

    /**
     * Description source: override, runtime, or none.
     */
    private String descriptionSource;

    /**
     * Operator remark.
     */
    private String remark;

    /**
     * Sort order in the management page.
     */
    private Integer sortOrder;
}
