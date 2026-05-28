package top.aiolife.mcp.api;

import cn.dev33.satoken.stp.StpUtil;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.pojo.req.ThoughtSaveToolEventReq;
import top.aiolife.mcp.pojo.req.ThoughtSaveToolReq;
import top.aiolife.record.pojo.req.ThoughtSaveReq;
import top.aiolife.record.pojo.req.ThoughtSaveEventReq;
import top.aiolife.record.service.IThoughtService;

import java.util.List;

/**
 * 闪念 MCP 工具控制器
 *
 * @author Ethan
 */
@RestController
@RequiredArgsConstructor
public class ThoughtToolController {

    private final IThoughtService thoughtService;

    @Tool("保存一条想法，并可附带多个关联事件")
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
