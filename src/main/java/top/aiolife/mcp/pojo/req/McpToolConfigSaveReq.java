package top.aiolife.mcp.pojo.req;

import lombok.Data;

/**
 * MCP tool configuration save request.
 *
 * <p>Receives editable display fields from the management page.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Data
public class McpToolConfigSaveReq {

    /**
     * Display name shown in the management page.
     */
    private String displayName;

    /**
     * Tool group name shown in the management page.
     */
    private String groupName;

    /**
     * Description override shown before runtime description.
     */
    private String descriptionOverride;

    /**
     * Sort order in the management page.
     */
    private Integer sortOrder;

    /**
     * Whether the tool may write or modify business data.
     */
    private Boolean writeOperation;

    /**
     * Operator remark.
     */
    private String remark;
}
