package top.aiolife.ai.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.aiolife.ai.pojo.req.AiChatReq;
import top.aiolife.ai.pojo.vo.AiChatResp;
import top.aiolife.ai.service.AiChatService;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;

/**
 * 统一 AI 聊天入口控制器。
 *
 * @author Ethan
 * @date 2026-07-19
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * 发送非流式 AI 聊天请求。
     *
     * <p>用途：为前端提供统一 AI 聊天入口，并为后续 Agent 编排扩展预留稳定入口。</p>
     *
     * @param req AI 聊天请求体，包含 Agent 编码、会话 id、用户消息和兼容旧逻辑的上下文
     * @return 统一返回结构，data 包含 Agent 编码、会话 id、助手回复内容和模型名称
     *
     * @author Ethan
     * @date 2026-06-28
     */
    @PostMapping("/chat")
    public ApiResponse<AiChatResp> chat(@RequestBody AiChatReq req) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(aiChatService.chat(userId, req));
        } catch (Exception e) {
            log.error("Failed to chat with AI: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 发送流式 AI 聊天请求。
     *
     * <p>用途：为前端提供统一 AI 流式聊天入口，通过 token、done 和 error 事件输出结构化 JSON。</p>
     *
     * @param req AI 聊天请求体，包含 Agent 编码、会话 id、用户消息和兼容旧逻辑的上下文
     * @return SSE 发送器，token 事件 data 包含 content，done 事件包含会话与模型信息，error 事件包含错误码和安全提示
     *
     * @author Ethan
     * @date 2026-07-19
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AiChatReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        return aiChatService.chatStream(userId, req);
    }
}
