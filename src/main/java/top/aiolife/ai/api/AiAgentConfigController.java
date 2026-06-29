package top.aiolife.ai.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.ai.pojo.req.AiAgentConfigSaveReq;
import top.aiolife.ai.pojo.req.AiAgentStatusReq;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;
import top.aiolife.ai.service.AiAgentConfigService;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;

import java.util.List;

/**
 * AI Agent 配置管理接口控制器。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/agents")
public class AiAgentConfigController {

    private final AiAgentConfigService aiAgentConfigService;

    /**
     * 查询当前用户可用的 AI Agent 配置列表。
     *
     * <p>用途：合并系统默认配置和当前用户覆盖配置，返回前端可展示和可选择的 Agent 列表。</p>
     *
     * @return 统一返回结构，data 为当前用户生效的 AI Agent 配置列表
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @GetMapping
    public ApiResponse<List<AiAgentConfigVO>> listAgents() {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiAgentConfigService.listEffectiveConfigs(userId));
        } catch (Exception e) {
            log.error("Failed to list AI agents: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 根据 Agent 编码查询当前用户的生效配置。
     *
     * <p>用途：前端进入某个 Agent 配置详情时，获取系统默认配置与用户覆盖配置合并后的结果。</p>
     *
     * @param code Agent 编码，来自路径参数
     * @return 统一返回结构，data 为当前用户生效的 AI Agent 配置
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @GetMapping("/{code}")
    public ApiResponse<AiAgentConfigVO> getAgent(@PathVariable String code) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiAgentConfigService.getEffectiveConfig(userId, code));
        } catch (Exception e) {
            log.error("Failed to get AI agent: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 保存当前用户对指定 AI Agent 的覆盖配置。
     *
     * <p>用途：前端提交 Agent 名称、描述、模型 Key、系统提示词等配置，后端保存为当前用户的覆盖配置。</p>
     *
     * @param code Agent 编码，来自路径参数，请求体中的 code 不参与覆盖
     * @param req AI Agent 配置保存请求体
     * @return 统一返回结构，data 为保存后的生效 AI Agent 配置
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @PostMapping("/{code}")
    public ApiResponse<AiAgentConfigVO> saveAgent(@PathVariable String code, @RequestBody AiAgentConfigSaveReq req) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiAgentConfigService.saveUserConfig(userId, code, req));
        } catch (Exception e) {
            log.error("Failed to save AI agent: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 更新当前用户对指定 AI Agent 的启用状态。
     *
     * <p>用途：前端启用或禁用某个 Agent，后端保存当前用户维度的状态配置。</p>
     *
     * @param code Agent 编码，来自路径参数
     * @param req AI Agent 状态更新请求体
     * @return 统一返回结构，data 为更新后的生效 AI Agent 配置
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @PutMapping("/{code}/status")
    public ApiResponse<AiAgentConfigVO> updateStatus(@PathVariable String code, @RequestBody AiAgentStatusReq req) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            Boolean enabled = req == null ? null : req.getEnabled();
            return ApiResponse.success(aiAgentConfigService.updateStatus(userId, code, enabled));
        } catch (Exception e) {
            log.error("Failed to update AI agent status: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }
}
