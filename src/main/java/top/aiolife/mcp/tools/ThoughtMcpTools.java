package top.aiolife.mcp.tools;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.pojo.req.ThoughtSaveToolEventReq;
import top.aiolife.mcp.pojo.req.ThoughtSaveToolReq;
import top.aiolife.record.pojo.req.ThoughtSaveEventReq;
import top.aiolife.record.pojo.req.ThoughtSaveReq;
import top.aiolife.record.service.IThoughtService;

import java.util.List;

/**
 * 闪念 MCP 工具。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Component
@RequiredArgsConstructor
public class ThoughtMcpTools {

    private final IThoughtService thoughtService;

    /**
     * 保存一条闪念。
     *
     * @param req 闪念保存工具请求，包含主题、内容、状态、主题色、事件流和幂等键
     * @return 统一返回结构，data 为是否保存成功
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @McpOperation(
            name = "thought_save",
            description = "保存一条想法，并可附带多个关联事件"
    )
    public ApiResponse<Boolean> thoughtSave(ThoughtSaveToolReq req) {
        ThoughtSaveReq saveReq = new ThoughtSaveReq();
        saveReq.setSubject(req.getSubject());
        saveReq.setContent(req.getContent());
        saveReq.setThemeKey(req.getThemeKey());
        saveReq.setStatus(req.getStatus());

        List<ThoughtSaveToolEventReq> events = req.getEvents();
        if (events != null) {
            saveReq.setEvents(events.stream().map(e -> {
                ThoughtSaveEventReq eventReq = new ThoughtSaveEventReq();
                eventReq.setContent(e.getContent());
                return eventReq;
            }).toList());
        }

        long userId = StpUtil.getLoginIdAsLong();
        return thoughtService.save(saveReq, userId, req.getIdempotencyKey());
    }
}
