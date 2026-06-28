package top.aiolife.mcp.tools;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.core.resq.PageResp;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.pojo.req.ArticleDetailToolReq;
import top.aiolife.mcp.pojo.req.ArticleQueryToolReq;
import top.aiolife.mcp.pojo.req.ArticleSaveToolReq;
import top.aiolife.mcp.pojo.vo.ArticleDetailToolVO;
import top.aiolife.mcp.pojo.vo.ArticleQueryToolVO;
import top.aiolife.record.pojo.req.ArticleSaveReq;
import top.aiolife.record.service.ArticleAiFacade;

/**
 * 文章 MCP 工具，提供当前用户文章查询、详情查看和新增文章能力。
 *
 * @author Ethan
 * @date 2026-06-25
 */
@Component
@RequiredArgsConstructor
public class ArticleMcpTools {

    private final ArticleAiFacade articleAiFacade;

    /**
     * 查询当前用户文章列表。
     *
     * @param req 文章查询请求，包含分页、关键词、状态、标签和分类筛选条件
     * @return 统一返回结构，data 为文章分页摘要，不包含正文全文
     *
     * @author Ethan
     * @date 2026-06-25
     */
    @McpOperation(
            name = "article_query",
            description = "查询当前用户文章列表，支持关键词、状态、标签、分类 ID 或未分类筛选；只返回摘要和元信息，不返回 Markdown 全文"
    )
    public ApiResponse<PageResp<ArticleQueryToolVO>> query(ArticleQueryToolReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleAiFacade.query(req, userId));
    }

    /**
     * 查询当前用户文章详情。
     *
     * @param req 文章详情请求，必须包含文章 ID
     * @return 统一返回结构，data 为文章详情，包含 Markdown 原文和纯文本内容
     *
     * @author Ethan
     * @date 2026-06-25
     */
    @McpOperation(
            name = "article_detail",
            description = "按文章 ID 查询当前用户文章详情，返回 Markdown 原文、纯文本内容、摘要、标签、分类、状态、字数和时间"
    )
    public ApiResponse<ArticleDetailToolVO> detail(ArticleDetailToolReq req) {
        ArticleDetailToolReq safeReq = req == null ? new ArticleDetailToolReq() : req;
        long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleAiFacade.detail(safeReq.getId(), userId));
    }

    /**
     * 新增当前用户文章。
     *
     * @param req 文章新增请求，包含标题、Markdown 原文、摘要、标签、状态、分类和幂等键
     * @return 统一返回结构，data 为新增后的文章详情
     *
     * @author Ethan
     * @date 2026-06-25
     */
    @McpOperation(
            name = "article_save",
            description = "新增当前用户文章；只支持创建新文章，不更新已有文章；plainTextContent 和 wordCount 由后端生成；可通过 idempotencyKey 防止外部 AI 重试导致重复写入"
    )
    public ApiResponse<ArticleDetailToolVO> save(ArticleSaveToolReq req) {
        ArticleSaveToolReq safeReq = req == null ? new ArticleSaveToolReq() : req;
        long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleAiFacade.create(toSaveReq(safeReq), userId, safeReq.getIdempotencyKey()));
    }

    private ArticleSaveReq toSaveReq(ArticleSaveToolReq req) {
        ArticleSaveReq saveReq = new ArticleSaveReq();
        saveReq.setCategoryId(req.getCategoryId());
        saveReq.setTitle(req.getTitle());
        saveReq.setSummary(req.getSummary());
        saveReq.setMarkdownContent(req.getMarkdownContent());
        saveReq.setTags(req.getTags());
        saveReq.setStatus(req.getStatus());
        return saveReq;
    }
}
