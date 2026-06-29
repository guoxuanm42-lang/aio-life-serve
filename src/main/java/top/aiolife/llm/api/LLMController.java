package top.aiolife.llm.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.aiolife.ai.pojo.req.AiChatReq;
import top.aiolife.ai.pojo.vo.AiChatResp;
import top.aiolife.ai.service.AiChatService;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.llm.pojo.entity.ChatMessageEntity;
import top.aiolife.llm.pojo.entity.ConversationEntity;
import top.aiolife.llm.service.ChatMessageService;
import top.aiolife.llm.service.ConversationService;
import top.aiolife.llm.service.LLMKeyService;
import top.aiolife.llm.service.LLMService;
import top.aiolife.record.service.ITimeRecordService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/llm")
public class LLMController {

    private final LLMService llmService;
    private final LLMKeyService llmKeyService;
    private final ITimeRecordService timeRecordService;
    private final ChatMessageService chatMessageService;
    private final ConversationService chatSessionService;
    private final AiChatService aiChatService;

    /**
     * 兼容旧路径的非流式 AI 聊天接口。
     *
     * <p>用途：保留前端现有 /llm/chat 调用方式，内部统一委托新 AI 编排服务处理 Agent、系统上下文、
     * 短期聊天记忆和长期记忆，并返回旧接口所需的纯文本回复内容。</p>
     *
     * @param request 聊天请求参数，支持 prompt、context、conversationId、agentCode
     * @return 统一返回结构，data 为助手回复文本
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @PostMapping("/chat")
    public ApiResponse<String> chat(@RequestBody Map<String, Object> request) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            AiChatResp resp = aiChatService.chat(userId, toAiChatReq(request));
            return ApiResponse.success(resp.getContent());
        } catch (Exception e) {
            log.error("Failed to chat with LLM: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 兼容旧路径的流式 AI 聊天接口。
     *
     * <p>用途：保留前端现有 /llm/chat/stream 调用方式，内部统一委托新 AI 编排服务处理上下文和消息保存，
     * 并继续输出 token、[DONE]、[ERROR] SSE 数据。</p>
     *
     * @param request 聊天请求参数，支持 prompt、context、conversationId、agentCode
     * @return SSE 发送器，用于流式返回助手回复 token
     *
     * @author Ethan
     * @date 2026-06-29
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> request) {
        long userId = StpUtil.getLoginIdAsLong();
        return aiChatService.chatStream(userId, toAiChatReq(request));
    }

    @PostMapping("/summarize/time-records")
    public ApiResponse<String> summarizeTimeRecords(@RequestBody Map<String, String> request) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            String type = request.get("type");

            LocalDate date = LocalDate.now();
            var timeRecords = timeRecordService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<top.aiolife.record.pojo.entity.TimeRecordEntity>()
                            .eq(top.aiolife.record.pojo.entity.TimeRecordEntity::getUserId, userId)
                            .eq(top.aiolife.record.pojo.entity.TimeRecordEntity::getDate, date)
                            .orderByDesc(top.aiolife.record.pojo.entity.TimeRecordEntity::getStartTime)
            );

            StringBuilder timeRecordsText = new StringBuilder();
            for (var record : timeRecords) {
                timeRecordsText.append(String.format("时间: %s-%s, 分类: %s, 标题: %s, 描述: %s\n",
                        record.getStartTime(), record.getEndTime(), record.getCategoryId(),
                        record.getTitle(), record.getDescription()));
            }

            var llmKey = llmKeyService.getDefaultLLMKey(userId);
            if (llmKey == null) {
                return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "请先配置大模型 API Key");
            }

            String summary = llmService.summarizeTimeRecords(
                    llmKey.getApiKey(),
                    llmKey.getBaseUrl(),
                    llmKey.getModelName(),
                    timeRecordsText.toString()
            );

            return ApiResponse.success(summary);
        } catch (Exception e) {
            log.error("Failed to summarize time records: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    @GetMapping("/chat/history")
    public ApiResponse<List<ChatMessageEntity>> getChatHistory(@RequestParam(required = false) Long conversationId) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            List<ChatMessageEntity> history;
            if (conversationId != null) {
                history = chatMessageService.listByconversationId(userId, conversationId);
            } else {
                history = chatMessageService.listByUserId(userId);
            }
            return ApiResponse.success(history);
        } catch (Exception e) {
            log.error("Failed to get chat history: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    @DeleteMapping("/chat/history")
    public ApiResponse<Void> clearChatHistory(@RequestParam(required = false) Long conversationId) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            if (conversationId != null) {
                chatMessageService.deleteByconversationId(userId, conversationId);
            } else {
                chatMessageService.deleteByUserId(userId);
            }
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("Failed to clear chat history: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    @GetMapping("/sessions")
    public ApiResponse<List<ConversationEntity>> getSessions() {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(chatSessionService.listByUserId(userId));
        } catch (Exception e) {
            log.error("Failed to get chat sessions: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    @PostMapping("/sessions")
    public ApiResponse<ConversationEntity> createSession(@RequestBody Map<String, String> request) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            String title = request.get("title");
            return ApiResponse.success(chatSessionService.createSession(userId, title));
        } catch (Exception e) {
            log.error("Failed to create chat session: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    @PutMapping("/sessions/{conversationId}")
    public ApiResponse<Void> updateSession(@PathVariable Long conversationId, @RequestBody Map<String, String> request) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            String title = request.get("title");
            chatSessionService.updateTitle(userId, conversationId, title);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("Failed to update chat session: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    @DeleteMapping("/sessions/{conversationId}")
    public ApiResponse<Void> deleteSession(@PathVariable Long conversationId) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            chatSessionService.deleteSession(userId, conversationId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("Failed to delete chat session: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    private AiChatReq toAiChatReq(Map<String, Object> request) {
        Map<String, Object> safeRequest = request == null ? Map.of() : request;
        AiChatReq req = new AiChatReq();
        req.setAgentCode(safeRequest.get("agentCode") == null ? null : safeRequest.get("agentCode").toString());
        req.setMessage(safeRequest.get("prompt") == null ? null : safeRequest.get("prompt").toString());
        req.setContext(safeRequest.get("context") == null ? null : safeRequest.get("context").toString());
        req.setConversationId(parseConversationId(safeRequest.get("conversationId")));
        return req;
    }

    private Long parseConversationId(Object conversationId) {
        return conversationId == null ? null : Long.valueOf(conversationId.toString());
    }
}
