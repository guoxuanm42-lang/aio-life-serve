package top.aiolife.llm.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.aiolife.ai.activity.service.AiActivitySummaryHistoryService;
import top.aiolife.ai.pojo.req.AiChatReq;
import top.aiolife.ai.pojo.vo.AiChatResp;
import top.aiolife.ai.service.AiChatService;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.llm.pojo.entity.ChatMessageEntity;
import top.aiolife.llm.pojo.entity.ConversationEntity;
import top.aiolife.llm.pojo.req.ConversationCreateReq;
import top.aiolife.llm.pojo.req.ConversationUpdateReq;
import top.aiolife.llm.pojo.req.LlmChatReq;
import top.aiolife.llm.pojo.req.TimeRecordSummaryReq;
import top.aiolife.llm.pojo.resp.ChatMessageResp;
import top.aiolife.llm.service.ChatMessageService;
import top.aiolife.llm.service.ConversationService;
import top.aiolife.record.service.ITimeRecordService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 大模型兼容接口与聊天会话管理控制器。
 *
 * @author Ethan
 * @date 2026-08-15
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/llm")
public class LLMController {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final ITimeRecordService timeRecordService;
    private final ChatMessageService chatMessageService;
    private final ConversationService chatSessionService;
    private final AiChatService aiChatService;
    private final AiActivitySummaryHistoryService activitySummaryHistoryService;

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
     * @date 2026-07-19
     */
    @PostMapping("/chat")
    public ApiResponse<String> chat(@RequestBody LlmChatReq request) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            AiChatResp resp = aiChatService.chat(userId, toAiChatReq(request));
            return ApiResponse.success(resp.getContent());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
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
     * @date 2026-07-19
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody LlmChatReq request) {
        long userId = StpUtil.getLoginIdAsLong();
        return aiChatService.chatStreamLegacy(userId, toAiChatReq(request));
    }

    /**
     * 总结当前用户今日或本周的时迹记录。
     *
     * <p>用途：查询指定日期范围内的时迹记录，将记录作为隐藏上下文交给会话绑定的 Agent，
     * 通过统一 AI 编排链路只调用一次模型，并保存用户指令和完全一致的助手回复。</p>
     *
     * @param request 总结请求，包含 today/week 类型和当前会话 id
     * @return 统一返回结构，data 为已持久化的 AI 聊天响应
     *
     * @author Ethan
     * @date 2026-07-20
     */
    @PostMapping("/summarize/time-records")
    public ApiResponse<AiChatResp> summarizeTimeRecords(@RequestBody TimeRecordSummaryReq request) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            String type = normalizeSummaryType(request == null ? null : request.getType());
            Long conversationId = request == null ? null : request.getConversationId();
            ConversationEntity session = chatSessionService.getOwnedSession(userId, conversationId);

            LocalDate endDate = LocalDate.now(BUSINESS_ZONE_ID);
            LocalDate startDate = "week".equals(type)
                    ? endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    : endDate;
            var timeRecords = timeRecordService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<top.aiolife.record.pojo.entity.TimeRecordEntity>()
                            .eq(top.aiolife.record.pojo.entity.TimeRecordEntity::getUserId, userId)
                            .between(top.aiolife.record.pojo.entity.TimeRecordEntity::getDate, startDate, endDate)
                            .orderByAsc(top.aiolife.record.pojo.entity.TimeRecordEntity::getDate)
                            .orderByDesc(top.aiolife.record.pojo.entity.TimeRecordEntity::getStartTime)
            );

            StringBuilder timeRecordsText = new StringBuilder();
            for (var record : timeRecords) {
                timeRecordsText.append(String.format("日期: %s, 时间: %s-%s, 分类: %s, 标题: %s, 描述: %s%n",
                        record.getDate(), record.getStartTime(), record.getEndTime(), record.getCategoryId(),
                        record.getTitle(), record.getDescription()));
            }

            AiChatReq chatReq = new AiChatReq();
            chatReq.setAgentCode(session.getAgentCode());
            chatReq.setConversationId(conversationId);
            chatReq.setMessage("today".equals(type) ? "总结今日时迹" : "总结本周时迹");
            chatReq.setContext(buildTimeRecordSummaryContext(startDate, endDate, timeRecordsText));
            return ApiResponse.success(aiChatService.chat(userId, chatReq));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to summarize time records: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 查询当前用户的聊天历史。
     *
     * <p>用途：按会话读取消息时先校验会话归属；未传会话 id 时兼容返回当前用户全部历史。</p>
     *
     * @param conversationId 可选会话 id
     * @return 统一返回结构，data 为按创建时间和消息 ID 稳定排序的消息列表
     *
     * @author Ethan
     * @date 2026-08-15
     */
    @GetMapping("/chat/history")
    public ApiResponse<List<ChatMessageResp>> getChatHistory(@RequestParam(required = false) Long conversationId) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            List<ChatMessageEntity> history;
            if (conversationId != null) {
                chatSessionService.getOwnedSession(userId, conversationId);
                history = chatMessageService.listByconversationId(userId, conversationId);
            } else {
                history = chatMessageService.listByUserId(userId);
            }
            return ApiResponse.success(activitySummaryHistoryService.assemble(userId, history));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to get chat history: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 清空当前用户的聊天历史。
     *
     * <p>用途：传入会话 id 时仅清空归属于当前用户的指定会话消息，否则清空当前用户全部消息。</p>
     *
     * @param conversationId 可选会话 id
     * @return 统一空成功响应
     *
     * @author Ethan
     * @date 2026-07-20
     */
    @DeleteMapping("/chat/history")
    public ApiResponse<Void> clearChatHistory(@RequestParam(required = false) Long conversationId) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            if (conversationId != null) {
                chatSessionService.getOwnedSession(userId, conversationId);
                chatMessageService.deleteByconversationId(userId, conversationId);
            } else {
                chatMessageService.deleteByUserId(userId);
            }
            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to clear chat history: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 查询当前用户的 AI 会话列表。
     *
     * @return 统一返回结构，data 为包含绑定 Agent 的会话列表
     *
     * @author Ethan
     * @date 2026-07-20
     */
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

    /**
     * 创建绑定指定 Agent 的 AI 会话。
     *
     * <p>用途：前端发送首条消息或触发总结前创建真实会话；未传 Agent 时兼容使用生活总助理。</p>
     *
     * @param request 创建请求，包含标题和可选 Agent 编码
     * @return 统一返回结构，data 为创建后的会话
     *
     * @author Ethan
     * @date 2026-07-20
     */
    @PostMapping("/sessions")
    public ApiResponse<ConversationEntity> createSession(@RequestBody ConversationCreateReq request) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            String title = request == null ? null : request.getTitle();
            String agentCode = request == null ? null : request.getAgentCode();
            return ApiResponse.success(chatSessionService.createSession(userId, title, agentCode));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create chat session: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 更新当前用户会话的标题。
     *
     * @param conversationId 会话 id
     * @param request 更新请求，包含新标题
     * @return 统一空成功响应
     *
     * @author Ethan
     * @date 2026-07-20
     */
    @PutMapping("/sessions/{conversationId}")
    public ApiResponse<Void> updateSession(@PathVariable Long conversationId, @RequestBody ConversationUpdateReq request) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            String title = request == null ? null : request.getTitle();
            chatSessionService.updateTitle(userId, conversationId, title);
            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update chat session: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    /**
     * 删除当前用户拥有的会话及其消息。
     *
     * @param conversationId 会话 id
     * @return 统一空成功响应
     *
     * @author Ethan
     * @date 2026-07-20
     */
    @DeleteMapping("/sessions/{conversationId}")
    public ApiResponse<Void> deleteSession(@PathVariable Long conversationId) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            chatSessionService.deleteSession(userId, conversationId);
            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to delete chat session: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
        }
    }

    private AiChatReq toAiChatReq(LlmChatReq request) {
        AiChatReq req = new AiChatReq();
        if (request == null) {
            return req;
        }
        req.setAgentCode(request.getAgentCode());
        req.setMessage(request.getPrompt());
        req.setContext(request.getContext());
        req.setConversationId(request.getConversationId());
        return req;
    }

    private String normalizeSummaryType(String type) {
        if (!"today".equals(type) && !"week".equals(type)) {
            throw new IllegalArgumentException("总结类型必须为 today 或 week");
        }
        return type;
    }

    private String buildTimeRecordSummaryContext(LocalDate startDate, LocalDate endDate, StringBuilder records) {
        String recordText = records.isEmpty() ? "（该日期范围内暂无时迹记录）" : records.toString();
        return String.format(
                "以下是 %s 至 %s 的时迹记录，仅作为本次总结的数据上下文。请分析时间分配、活动类型、效率并给出建议。%n%s",
                startDate,
                endDate,
                recordText
        );
    }
}
