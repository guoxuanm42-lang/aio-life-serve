package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.record.pojo.entity.ProblemCategoryEntity;
import top.aiolife.record.pojo.req.ProblemCategorySaveReq;
import top.aiolife.record.pojo.vo.ProblemCategoryListVO;
import top.aiolife.record.service.IProblemCategoryService;

/**
 * 题目分类控制器，提供分类列表、新增、编辑和删除接口。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/problem-category")
public class ProblemCategoryController {

    private final IProblemCategoryService problemCategoryService;

    /**
     * 查询当前用户题目分类列表接口。
     *
     * <p>用途：前端题目分类首页展示全部题目、未分类和用户分类卡片及题目数量。</p>
     *
     * @return 统一返回结构，data 包含分类列表和题目数量
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @GetMapping("/list")
    public ApiResponse<ProblemCategoryListVO> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(problemCategoryService.listWithCount(userId));
    }

    /**
     * 新增题目分类接口。
     *
     * <p>用途：前端提交分类名称，后端创建当前用户的题目分类。</p>
     *
     * @param req 分类保存请求
     * @return 统一返回结构，data 为新增后的分类
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @PostMapping("/save")
    public ApiResponse<ProblemCategoryEntity> save(@RequestBody ProblemCategorySaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(problemCategoryService.create(req, userId));
    }

    /**
     * 更新题目分类接口。
     *
     * <p>用途：前端修改分类名称或排序值，后端按当前用户校验归属后更新。</p>
     *
     * @param req 分类保存请求，必须包含 id
     * @return 统一返回结构，data 为更新后的分类
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @PostMapping("/update")
    public ApiResponse<ProblemCategoryEntity> update(@RequestBody ProblemCategorySaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(problemCategoryService.update(req, userId));
    }

    /**
     * 删除题目分类接口。
     *
     * <p>用途：前端删除分类，后端将分类下题目改为未分类并逻辑删除分类。</p>
     *
     * @param req 删除请求，必须包含 id
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-06-22
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody ProblemCategorySaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = req == null ? null : req.getId();
        problemCategoryService.delete(id, userId);
        return ApiResponse.success();
    }
}
