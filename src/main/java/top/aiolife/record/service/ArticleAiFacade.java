package top.aiolife.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.core.resq.PageResp;
import top.aiolife.mcp.pojo.req.ArticleQueryToolReq;
import top.aiolife.mcp.pojo.vo.ArticleDetailToolVO;
import top.aiolife.mcp.pojo.vo.ArticleQueryToolVO;
import top.aiolife.record.pojo.req.ArticleQueryReq;
import top.aiolife.record.pojo.req.ArticleSaveReq;
import top.aiolife.record.pojo.vo.ArticleDetailVO;
import top.aiolife.record.pojo.vo.ArticleListVO;
import top.aiolife.record.util.RedisUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 文章 MCP/AI 适配门面，负责文章查询、详情、新增和幂等控制。
 *
 * @author Ethan
 * @date 2026-06-25
 */
@Component
@RequiredArgsConstructor
public class ArticleAiFacade {

    private static final long IDEMPOTENCY_TTL_SECONDS = TimeUnit.HOURS.toSeconds(24);

    private static final String LOCK_VALUE = "LOCK";

    private final IArticleService articleService;

    private final RedisUtil redisUtil;

    /**
     * 查询当前用户文章列表，并转换为 MCP 专用摘要结构。
     *
     * @param req MCP 查询请求
     * @param userId 当前用户 ID
     * @return 文章分页摘要
     *
     * @author Ethan
     * @date 2026-06-25
     */
    public PageResp<ArticleQueryToolVO> query(ArticleQueryToolReq req, long userId) {
        PageResp<ArticleListVO> page = articleService.query(toQueryReq(req), userId);
        List<ArticleQueryToolVO> items = page.getItems() == null
                ? List.of()
                : page.getItems().stream().map(ArticleQueryToolVO::of).toList();
        return PageResp.of(items, page.getTotal());
    }

    /**
     * 查询当前用户文章详情，并返回 Markdown 原文和纯文本内容。
     *
     * @param id 文章 ID
     * @param userId 当前用户 ID
     * @return 文章详情
     *
     * @author Ethan
     * @date 2026-06-25
     */
    public ArticleDetailToolVO detail(Long id, long userId) {
        return ArticleDetailToolVO.of(articleService.detail(id, userId));
    }

    /**
     * 新增当前用户文章，支持基于幂等键避免重复写入。
     *
     * @param req 文章保存请求
     * @param userId 当前用户 ID
     * @param idempotencyKey 幂等键
     * @return 新增后的文章详情
     *
     * @author Ethan
     * @date 2026-06-25
     */
    public ArticleDetailToolVO create(ArticleSaveReq req, long userId, String idempotencyKey) {
        String idempotencyRedisKey = buildIdempotencyKey(userId, idempotencyKey);
        if (idempotencyRedisKey == null) {
            return ArticleDetailToolVO.of(articleService.create(req, userId));
        }

        String existing = redisUtil.get(idempotencyRedisKey);
        if (StringUtils.hasText(existing) && !LOCK_VALUE.equals(existing)) {
            return detail(Long.parseLong(existing), userId);
        }

        Boolean locked = redisUtil.setIfAbsent(idempotencyRedisKey, LOCK_VALUE, IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            String savedId = redisUtil.get(idempotencyRedisKey);
            if (StringUtils.hasText(savedId) && !LOCK_VALUE.equals(savedId)) {
                return detail(Long.parseLong(savedId), userId);
            }
            return null;
        }

        try {
            ArticleDetailVO created = articleService.create(req, userId);
            redisUtil.set(idempotencyRedisKey, String.valueOf(created.getId()), IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
            return ArticleDetailToolVO.of(created);
        } catch (Exception exception) {
            redisUtil.delete(idempotencyRedisKey);
            throw exception;
        }
    }

    private ArticleQueryReq toQueryReq(ArticleQueryToolReq req) {
        ArticleQueryToolReq safeReq = req == null ? new ArticleQueryToolReq() : req;
        ArticleQueryReq queryReq = new ArticleQueryReq();
        queryReq.setPage(safeReq.getPage());
        queryReq.setPageSize(safeReq.getPageSize());
        queryReq.setKeyword(safeReq.getKeyword());
        queryReq.setStatus(safeReq.getStatus());
        queryReq.setTags(safeReq.getTags());
        queryReq.setCategoryId(safeReq.getCategoryId());
        queryReq.setUncategorized(safeReq.getUncategorized());
        return queryReq;
    }

    private String buildIdempotencyKey(long userId, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return null;
        }
        return "mcp:idemp:article_save:" + userId + ":" + idempotencyKey.trim();
    }
}
