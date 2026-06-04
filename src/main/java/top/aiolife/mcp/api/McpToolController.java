package top.aiolife.mcp.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.mcp.pojo.entity.McpToolCallLogEntity;
import top.aiolife.mcp.pojo.entity.McpToolConfigEntity;
import top.aiolife.mcp.pojo.req.McpToolCallReq;
import top.aiolife.mcp.pojo.req.McpToolConfigSaveReq;
import top.aiolife.mcp.pojo.req.McpToolStatusReq;
import top.aiolife.mcp.pojo.vo.McpToolVO;
import top.aiolife.mcp.registry.McpToolRegistry;
import top.aiolife.mcp.service.IMcpToolCallLogService;
import top.aiolife.mcp.service.IMcpToolCallService;
import top.aiolife.mcp.service.IMcpToolConfigService;

import java.util.List;

/**
 * MCP tool management controller.
 *
 * <p>Provides REST APIs for listing runtime MCP tools, editing operation configuration, testing calls, and reading
 * audit logs.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mcp/tools")
public class McpToolController {

    private final McpToolRegistry mcpToolRegistry;
    private final IMcpToolConfigService mcpToolConfigService;
    private final IMcpToolCallService mcpToolCallService;
    private final IMcpToolCallLogService mcpToolCallLogService;

    /**
     * Lists MCP tools for the management page.
     *
     * <p>Purpose: the frontend reads runtime tools merged with database operation configuration, including schema,
     * display metadata, enablement, risk flag, and registration status.</p>
     *
     * @return unified response whose data is the merged MCP tool list
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @GetMapping
    public ApiResponse<List<McpToolVO>> listTools() {
        return ApiResponse.success(mcpToolConfigService.listMergedTools(mcpToolRegistry.getAllTools()));
    }

    /**
     * Calls one MCP tool from the management page.
     *
     * <p>Purpose: the frontend submits arguments for a registered and enabled tool, then receives the raw MCP
     * call result. The shared call service handles enablement checks, rate limiting, invocation, and audit logging.</p>
     *
     * @param name tool name from path variable
     * @param req request body whose arguments field is passed to the MCP tool
     * @return unified response whose data is the MCP tool call result
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @PostMapping("/{name}/call")
    public ApiResponse<McpSchema.CallToolResult> callTool(@PathVariable("name") String name,
                                                          @RequestBody McpToolCallReq req) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        McpSchema.CallToolResult result = mcpToolCallService.invoke(
                name,
                req == null ? null : req.getArguments(),
                currentLoginId(),
                requestAttributes
        );
        return ApiResponse.success(result);
    }

    /**
     * Saves display and risk configuration for one MCP tool.
     *
     * <p>Purpose: administrators update display name, group, description override, sort order, write-operation flag,
     * and remark for a global MCP tool resource.</p>
     *
     * @param name tool name from path variable
     * @param req request body containing editable operation configuration fields
     * @return unified response whose data is the saved configuration entity
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @SaCheckRole("admin")
    @PutMapping("/{name}/config")
    public ApiResponse<McpToolConfigEntity> saveConfig(@PathVariable("name") String name,
                                                       @RequestBody McpToolConfigSaveReq req) {
        try {
            return ApiResponse.success(mcpToolConfigService.saveConfig(name, req));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, exception.getMessage());
        }
    }

    /**
     * Updates enablement status for one MCP tool.
     *
     * <p>Purpose: administrators enable or disable a global MCP tool resource. Disabled tools are hidden from MCP
     * protocol tools/list and cannot be called through the shared call service.</p>
     *
     * @param name tool name from path variable
     * @param req request body containing enabled status
     * @return unified response whose data is the saved configuration entity
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @SaCheckRole("admin")
    @PutMapping("/{name}/status")
    public ApiResponse<McpToolConfigEntity> updateStatus(@PathVariable("name") String name,
                                                         @RequestBody McpToolStatusReq req) {
        try {
            return ApiResponse.success(mcpToolConfigService.updateStatus(name, req == null ? null : req.getEnabled()));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, exception.getMessage());
        }
    }

    /**
     * Lists recent MCP tool call audit logs.
     *
     * <p>Purpose: administrators inspect recent REST simulation and MCP protocol calls for one MCP tool.</p>
     *
     * @param name tool name from path variable
     * @param limit maximum rows to return, default 50 and maximum 200
     * @return unified response whose data is recent audit log list
     *
     * @author Ethan
     * @date 2026-06-03
     */
    @SaCheckRole("admin")
    @GetMapping("/{name}/logs")
    public ApiResponse<List<McpToolCallLogEntity>> listLogs(@PathVariable("name") String name,
                                                            @RequestParam(value = "limit", defaultValue = "50")
                                                            Integer limit) {
        return ApiResponse.success(mcpToolCallLogService.listRecentLogs(name, limit == null ? 50 : limit));
    }

    private Object currentLoginId() {
        try {
            return StpUtil.getLoginIdDefaultNull();
        } catch (Exception exception) {
            return null;
        }
    }
}
