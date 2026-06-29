package top.aiolife.ai.memory.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.ai.memory.pojo.req.AiMemorySaveReq;
import top.aiolife.ai.memory.pojo.req.AiMemoryStatusReq;
import top.aiolife.ai.memory.pojo.vo.AiMemoryVO;
import top.aiolife.ai.memory.service.AiMemoryService;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;

import java.util.List;

/**
 * AI 长期记忆管理接口控制器。
 *
 * @author Ethan
 * @date 2026-06-28
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/memories")
public class AiMemoryController {

    private final AiMemoryService aiMemoryService;

    /**
     * 查询当前用户的长期记忆列表。
     *
     * <p>用途：前端按 Agent 或记忆类型筛选当前用户长期记忆。</p>
     *
     * @param agentCode Agent 编码，可为空
     * @param memoryType 记忆类型，可为空
     * @return 统一返回结构，data 为长期记忆列表
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @GetMapping
    public ApiResponse<List<AiMemoryVO>> listMemories(
            @RequestParam(required = false) String agentCode,
            @RequestParam(required = false) String memoryType
    ) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiMemoryService.listMemories(userId, agentCode, memoryType));
        } catch (Exception e) {
            log.error("查询 AI 长期记忆失败: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 查询当前用户的一条长期记忆。
     *
     * <p>用途：前端查看或编辑单条长期记忆时获取详情。</p>
     *
     * @param id 记忆 id
     * @return 统一返回结构，data 为长期记忆详情
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @GetMapping("/{id}")
    public ApiResponse<AiMemoryVO> getMemory(@PathVariable Long id) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiMemoryService.getMemory(userId, id));
        } catch (Exception e) {
            log.error("查询 AI 长期记忆详情失败: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 创建当前用户的长期记忆。
     *
     * <p>用途：前端或业务规则提交长期记忆内容，后端保存到用户维度的记忆表。</p>
     *
     * @param req 长期记忆保存请求体
     * @return 统一返回结构，data 为保存后的长期记忆
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @PostMapping
    public ApiResponse<AiMemoryVO> saveMemory(@RequestBody AiMemorySaveReq req) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiMemoryService.saveMemory(userId, req));
        } catch (Exception e) {
            log.error("保存 AI 长期记忆失败: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 更新当前用户的长期记忆。
     *
     * <p>用途：前端提交单条长期记忆的修改内容，后端校验归属后更新。</p>
     *
     * @param id 记忆 id
     * @param req 长期记忆保存请求体
     * @return 统一返回结构，data 为更新后的长期记忆
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @PutMapping("/{id}")
    public ApiResponse<AiMemoryVO> updateMemory(@PathVariable Long id, @RequestBody AiMemorySaveReq req) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiMemoryService.updateMemory(userId, id, req));
        } catch (Exception e) {
            log.error("更新 AI 长期记忆失败: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 更新当前用户长期记忆的启用状态。
     *
     * <p>用途：前端启用或禁用某条长期记忆，不进行物理删除。</p>
     *
     * @param id 记忆 id
     * @param req 状态更新请求体
     * @return 统一返回结构，data 为更新后的长期记忆
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @PutMapping("/{id}/status")
    public ApiResponse<AiMemoryVO> updateStatus(@PathVariable Long id, @RequestBody AiMemoryStatusReq req) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            Boolean enabled = req == null ? null : req.getEnabled();
            return ApiResponse.success(aiMemoryService.updateStatus(userId, id, enabled));
        } catch (Exception e) {
            log.error("更新 AI 长期记忆状态失败: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }
}
