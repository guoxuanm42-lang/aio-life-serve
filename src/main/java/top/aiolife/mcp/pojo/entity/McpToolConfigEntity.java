package top.aiolife.mcp.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MCP tool operation configuration entity.
 *
 * <p>Stores global display, grouping, sorting, and enablement configuration for runtime MCP tools.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Data
@TableName("mcp_tool_config")
public class McpToolConfigEntity {

    /**
     * Primary key.
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Runtime tool name.
     */
    private String toolName;

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
     * Whether the tool is enabled.
     */
    private Boolean enabled;

    /**
     * Whether the tool may write or modify business data.
     */
    private Boolean writeOperation;

    /**
     * Sort order in the management page.
     */
    private Integer sortOrder;

    /**
     * Operator remark.
     */
    private String remark;

    /**
     * Creation time.
     */
    private LocalDateTime createTime;

    /**
     * Last update time.
     */
    private LocalDateTime updateTime;
}
