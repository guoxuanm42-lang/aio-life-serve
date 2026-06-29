package top.aiolife.ai.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.aiolife.ai.pojo.req.AiChatReq;
import top.aiolife.ai.pojo.vo.AiChatResp;

/**
 * AI 聊天编排服务接口。
 *
 * @author Ethan
 * @date 2026-06-28
 */
public interface AiChatService {

    /**
     * 发送非流式 AI 聊天请求，并保存用户消息和助手回复。
     *
     * @param userId 当前登录用户 id
     * @param req AI 聊天请求，包含 Agent 编码、会话 id、用户消息和兼容旧逻辑的上下文
     * @return AI 聊天响应，包含 Agent 编码、会话 id、回复内容和模型名称
     *
     * @author Ethan
     * @date 2026-06-28
     */
    AiChatResp chat(Long userId, AiChatReq req);

    /**
     * 发送流式 AI 聊天请求，并保存用户消息和助手回复。
     *
     * @param userId 当前登录用户 id
     * @param req AI 聊天请求，包含 Agent 编码、会话 id、用户消息和兼容旧逻辑的上下文
     * @return SSE 发送器，用于输出模型 token 和完成标记
     *
     * @author Ethan
     * @date 2026-06-28
     */
    SseEmitter chatStream(Long userId, AiChatReq req);
}
