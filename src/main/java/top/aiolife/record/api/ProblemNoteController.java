package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;
import top.aiolife.record.pojo.req.ProblemNoteQueryReq;
import top.aiolife.record.pojo.req.ProblemNoteSaveReq;
import top.aiolife.record.service.IProblemNoteService;

/**
 * 题目记录控制器，提供题目、Java 解法代码和思路备注的增删改查接口。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/problem-note")
public class ProblemNoteController {

    private final IProblemNoteService problemNoteService;

    /**
     * 分页查询当前用户的题目记录接口。
     *
     * <p>用途：前端按关键词、难度、状态和标签筛选题目记录列表。</p>
     *
     * @param req 题目记录查询请求
     * @return 统一返回结构，data.items 为题目记录列表，data.total 为总数
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @PostMapping("/query")
    public ApiResponse<PageResp<ProblemNoteEntity>> query(@RequestBody(required = false) ProblemNoteQueryReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(problemNoteService.query(req, userId));
    }

    /**
     * 查询当前用户的题目记录详情接口。
     *
     * <p>用途：前端打开题目详情时获取题目内容、Java 解法代码和思路备注。</p>
     *
     * @param id 题目记录 ID
     * @return 统一返回结构，data 为题目记录详情
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @GetMapping("/detail")
    public ApiResponse<ProblemNoteEntity> detail(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(problemNoteService.detail(id, userId));
    }

    /**
     * 新增题目记录接口。
     *
     * <p>用途：前端提交题目标题、题目内容、Java 代码和思路备注，后端创建当前用户的题目记录。</p>
     *
     * @param req 题目记录保存请求
     * @return 统一返回结构，data 为新增后的题目记录
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @PostMapping("/save")
    public ApiResponse<ProblemNoteEntity> save(@RequestBody ProblemNoteSaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(problemNoteService.create(req, userId));
    }

    /**
     * 更新题目记录接口。
     *
     * <p>用途：前端保存已有题目的题目内容、Java 代码和思路备注，后端按当前用户校验归属后更新。</p>
     *
     * @param req 题目记录保存请求，必须包含 id
     * @return 统一返回结构，data 为更新后的题目记录
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @PostMapping("/update")
    public ApiResponse<ProblemNoteEntity> update(@RequestBody ProblemNoteSaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(problemNoteService.update(req, userId));
    }

    /**
     * 删除题目记录接口。
     *
     * <p>用途：前端删除当前用户的题目记录，后端执行逻辑删除。</p>
     *
     * @param entity 删除请求，必须包含 id
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody ProblemNoteEntity entity) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = entity == null ? null : entity.getId();
        problemNoteService.delete(id, userId);
        return ApiResponse.success();
    }
}
