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
import top.aiolife.record.pojo.entity.ArticleCategoryEntity;
import top.aiolife.record.pojo.req.ArticleCategorySaveReq;
import top.aiolife.record.pojo.vo.ArticleCategoryListVO;
import top.aiolife.record.service.IArticleCategoryService;

/**
 * 文章分类控制器，提供分类列表、新增、编辑和删除接口。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/article-category")
public class ArticleCategoryController {

    private final IArticleCategoryService articleCategoryService;

    /**
     * 查询当前用户文章分类列表接口。
     *
     * <p>用途：前端展示全部文章、未分类文章和用户分类卡片及文章数量。</p>
     *
     * @return 统一返回结构，data 包含分类列表和文章数量
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @GetMapping("/list")
    public ApiResponse<ArticleCategoryListVO> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleCategoryService.listWithCount(userId));
    }

    /**
     * 新增文章分类接口。
     *
     * <p>用途：前端提交分类名称和排序值，后端创建当前用户文章分类。</p>
     *
     * @param req 分类保存请求
     * @return 统一返回结构，data 为新增后的文章分类
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @PostMapping("/save")
    public ApiResponse<ArticleCategoryEntity> save(@RequestBody ArticleCategorySaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleCategoryService.create(req, userId));
    }

    /**
     * 更新文章分类接口。
     *
     * <p>用途：前端修改分类名称或排序值，后端按当前用户校验归属后更新。</p>
     *
     * @param req 分类保存请求，必须包含 id
     * @return 统一返回结构，data 为更新后的文章分类
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @PostMapping("/update")
    public ApiResponse<ArticleCategoryEntity> update(@RequestBody ArticleCategorySaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleCategoryService.update(req, userId));
    }

    /**
     * 删除文章分类接口。
     *
     * <p>用途：前端删除分类，后端将该分类下文章转为未分类并逻辑删除分类。</p>
     *
     * @param req 删除请求，必须包含 id
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody ArticleCategorySaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = req == null ? null : req.getId();
        articleCategoryService.delete(id, userId);
        return ApiResponse.success();
    }
}
