package top.aiolife.mcp.tools;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.core.resq.PageResp;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.pojo.req.ProblemNoteQueryToolReq;
import top.aiolife.mcp.pojo.req.ProblemNoteSaveToolReq;
import top.aiolife.mcp.pojo.vo.ProblemNoteQueryToolVO;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;
import top.aiolife.record.pojo.req.ProblemNoteSaveReq;
import top.aiolife.record.service.ProblemNoteAiFacade;

/**
 * 题目记录 MCP 工具，提供当前用户题库查询和新增题目能力。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Component
@RequiredArgsConstructor
public class ProblemNoteMcpTools {

    private final ProblemNoteAiFacade problemNoteAiFacade;

    /**
     * 查询当前用户题库。
     *
     * @param req 题目记录查询请求，包含分页、关键词、难度、状态、标签和分类筛选条件
     * @return 统一返回结构，data 为题目记录分页结果
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @McpOperation(
            name = "problem_note_query",
            description = "查询当前用户题库，支持按关键词、难度、状态、标签、分类 ID 或未分类筛选，返回题目内容、Java 解法代码和思路备注"
    )
    public ApiResponse<PageResp<ProblemNoteQueryToolVO>> query(ProblemNoteQueryToolReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(problemNoteAiFacade.query(req, userId));
    }

    /**
     * 新增当前用户题目记录。
     *
     * @param req 题目记录新增请求，包含题目标题、题目内容、Java 代码、思路、分类和幂等键
     * @return 统一返回结构，data 为新增后的题目记录
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @McpOperation(
            name = "problem_note_save",
            description = "新增当前用户题目记录；只支持创建新题目，不更新已有题目；可通过 idempotencyKey 防止外部 AI 重试导致重复写入"
    )
    public ApiResponse<ProblemNoteEntity> save(ProblemNoteSaveToolReq req) {
        ProblemNoteSaveToolReq safeReq = req == null ? new ProblemNoteSaveToolReq() : req;
        long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(problemNoteAiFacade.create(toSaveReq(safeReq), userId, safeReq.getIdempotencyKey()));
    }

    private ProblemNoteSaveReq toSaveReq(ProblemNoteSaveToolReq req) {
        ProblemNoteSaveReq saveReq = new ProblemNoteSaveReq();
        saveReq.setCategoryId(req.getCategoryId());
        saveReq.setTitle(req.getTitle());
        saveReq.setProblemContent(req.getProblemContent());
        saveReq.setSolutionCode(req.getSolutionCode());
        saveReq.setPseudoCode(req.getPseudoCode());
        saveReq.setIdeaNote(req.getIdeaNote());
        saveReq.setDifficulty(req.getDifficulty());
        saveReq.setTags(req.getTags());
        saveReq.setStatus(req.getStatus());
        return saveReq;
    }
}
