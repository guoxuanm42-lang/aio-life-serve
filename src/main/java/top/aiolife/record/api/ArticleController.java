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
import top.aiolife.record.pojo.entity.ArticleEntity;
import top.aiolife.record.pojo.req.ArticleQueryReq;
import top.aiolife.record.pojo.req.ArticleSaveReq;
import top.aiolife.record.pojo.vo.ArticleDetailVO;
import top.aiolife.record.pojo.vo.ArticleListVO;
import top.aiolife.record.service.IArticleService;

/**
 * 文章控制器，提供文章列表、详情、新增、编辑和删除接口。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/article")
public class ArticleController {

    private final IArticleService articleService;

    /**
     * 分页查询当前用户文章接口。
     *
     * <p>用途：前端按标题、正文、分类、状态或标签筛选文章列表，返回轻量文章元数据。</p>
     *
     * @param req 文章查询请求
     * @return 统一返回结构，data.items 为文章列表，data.total 为总数
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @PostMapping("/query")
    public ApiResponse<PageResp<ArticleListVO>> query(@RequestBody(required = false) ArticleQueryReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleService.query(req, userId));
    }

    /**
     * 查询当前用户文章详情接口。
     *
     * <p>用途：前端打开文章详情时获取 Markdown 原文和纯文本内容。</p>
     *
     * @param id 文章 ID
     * @return 统一返回结构，data 为文章详情
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @GetMapping("/detail")
    public ApiResponse<ArticleDetailVO> detail(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleService.detail(id, userId));
    }

    /**
     * 新增文章接口。
     *
     * <p>用途：前端提交文章标题、摘要、Markdown 正文、分类、标签和状态，后端创建当前用户文章并派生纯文本和字数。</p>
     *
     * @param req 文章保存请求
     * @return 统一返回结构，data 为新增后的文章详情
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @PostMapping("/save")
    public ApiResponse<ArticleDetailVO> save(@RequestBody ArticleSaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleService.create(req, userId));
    }

    /**
     * 更新文章接口。
     *
     * <p>用途：前端保存已有文章修改，后端校验归属后更新文章并重新派生纯文本和字数。</p>
     *
     * @param req 文章保存请求，必须包含 id
     * @return 统一返回结构，data 为更新后的文章详情
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @PostMapping("/update")
    public ApiResponse<ArticleDetailVO> update(@RequestBody ArticleSaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleService.update(req, userId));
    }

    /**
     * 删除文章接口。
     *
     * <p>用途：前端删除当前用户文章，后端执行逻辑删除。</p>
     *
     * @param entity 删除请求，必须包含 id
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody ArticleEntity entity) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = entity == null ? null : entity.getId();
        articleService.delete(id, userId);
        return ApiResponse.success();
    }
}
