package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.mapper.IArticleCategoryMapper;
import top.aiolife.record.mapper.IArticleMapper;
import top.aiolife.record.pojo.entity.ArticleCategoryEntity;
import top.aiolife.record.pojo.entity.ArticleEntity;
import top.aiolife.record.pojo.req.ArticleQueryReq;
import top.aiolife.record.pojo.req.ArticleSaveReq;
import top.aiolife.record.pojo.vo.ArticleDetailVO;
import top.aiolife.record.pojo.vo.ArticleListVO;
import top.aiolife.record.service.IArticleService;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 文章服务实现，按当前用户隔离文章并维护 AI 分析可用的纯文本和字数字段。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<IArticleMapper, ArticleEntity> implements IArticleService {

    private static final String DEFAULT_STATUS = "draft";

    private static final Set<String> ALLOWED_STATUSES = Set.of("draft", "published", "archived");

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private final IArticleMapper articleMapper;

    private final IArticleCategoryMapper articleCategoryMapper;

    /**
     * 分页查询当前用户的文章列表。
     *
     * @param req 查询请求
     * @param userId 当前用户 ID
     * @return 文章列表分页数据
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @Override
    public PageResp<ArticleListVO> query(ArticleQueryReq req, Long userId) {
        ArticleQueryReq safeReq = req == null ? new ArticleQueryReq() : req;
        LambdaQueryWrapper<ArticleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleEntity::getUserId, userId);
        if (StringUtils.hasText(safeReq.getKeyword())) {
            String keyword = safeReq.getKeyword().trim();
            wrapper.and(item -> item
                    .like(ArticleEntity::getTitle, keyword)
                    .or()
                    .like(ArticleEntity::getSummary, keyword)
                    .or()
                    .like(ArticleEntity::getPlainTextContent, keyword)
                    .or()
                    .like(ArticleEntity::getTags, keyword));
        }
        if (Boolean.TRUE.equals(safeReq.getUncategorized())) {
            wrapper.isNull(ArticleEntity::getCategoryId);
        } else if (safeReq.getCategoryId() != null) {
            wrapper.eq(ArticleEntity::getCategoryId, safeReq.getCategoryId());
        }
        String status = normalizeStatus(safeReq.getStatus(), false);
        if (status != null) {
            wrapper.eq(ArticleEntity::getStatus, status);
        }
        if (StringUtils.hasText(safeReq.getTags())) {
            wrapper.like(ArticleEntity::getTags, safeReq.getTags().trim());
        }
        wrapper.orderByDesc(ArticleEntity::getUpdateTime);

        Page<ArticleEntity> page = new Page<>(normalizePage(safeReq.getPage()), normalizePageSize(safeReq.getPageSize()));
        IPage<ArticleEntity> iPage = articleMapper.selectPage(page, wrapper);
        return PageResp.of(iPage.getRecords().stream().map(ArticleListVO::of).toList(), iPage.getTotal());
    }

    /**
     * 查询当前用户的文章详情。
     *
     * @param id 文章 ID
     * @param userId 当前用户 ID
     * @return 文章详情
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @Override
    public ArticleDetailVO detail(Long id, Long userId) {
        return ArticleDetailVO.of(getOwnedArticle(id, userId));
    }

    /**
     * 新增当前用户的文章。
     *
     * @param req 保存请求
     * @param userId 当前用户 ID
     * @return 新增后的文章详情
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleDetailVO create(ArticleSaveReq req, Long userId) {
        ArticleEntity entity = buildEntity(req, userId, false);
        entity.fillCreateCommonField(userId);
        articleMapper.insert(entity);
        return ArticleDetailVO.of(entity);
    }

    /**
     * 更新当前用户的文章。
     *
     * @param req 保存请求，必须包含文章 ID
     * @param userId 当前用户 ID
     * @return 更新后的文章详情
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleDetailVO update(ArticleSaveReq req, Long userId) {
        if (req == null || req.getId() == null) {
            throw new IllegalArgumentException("文章 ID 不能为空");
        }
        getOwnedArticle(req.getId(), userId);
        ArticleEntity entity = buildEntity(req, userId, true);
        entity.setId(req.getId());
        entity.fillUpdateCommonField(userId);
        articleMapper.updateById(entity);
        return detail(req.getId(), userId);
    }

    /**
     * 逻辑删除当前用户的文章。
     *
     * @param id 文章 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-06-24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        ArticleEntity entity = getOwnedArticle(id, userId);
        entity.setIsDeleted(StatusConst.IS_DELETE);
        entity.fillUpdateCommonField(userId);
        articleMapper.updateById(entity);
    }

    private ArticleEntity buildEntity(ArticleSaveReq req, Long userId, boolean update) {
        if (req == null) {
            throw new IllegalArgumentException("文章不能为空");
        }
        String markdownContent = normalizeRequired(req.getMarkdownContent(), "Markdown 正文不能为空");
        String plainTextContent = toPlainText(markdownContent);
        ArticleEntity entity = new ArticleEntity();
        entity.setUserId(update ? null : userId);
        entity.setCategoryId(resolveCategoryId(req.getCategoryId(), userId));
        entity.setTitle(normalizeRequired(req.getTitle(), "文章标题不能为空"));
        entity.setSummary(normalizeBlank(req.getSummary()));
        entity.setMarkdownContent(markdownContent);
        entity.setPlainTextContent(plainTextContent);
        entity.setTags(normalizeBlank(req.getTags()));
        entity.setStatus(normalizeStatus(req.getStatus(), true));
        entity.setWordCount(countWords(plainTextContent));
        return entity;
    }

    private Long resolveCategoryId(Long categoryId, Long userId) {
        if (categoryId == null) {
            return null;
        }
        Long count = articleCategoryMapper.selectCount(new LambdaQueryWrapper<ArticleCategoryEntity>()
                .eq(ArticleCategoryEntity::getId, categoryId)
                .eq(ArticleCategoryEntity::getUserId, userId));
        if (count == null || count == 0) {
            throw new IllegalArgumentException("文章分类不存在或无权访问");
        }
        return categoryId;
    }

    private ArticleEntity getOwnedArticle(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("文章 ID 不能为空");
        }
        ArticleEntity entity = articleMapper.selectOne(new LambdaQueryWrapper<ArticleEntity>()
                .eq(ArticleEntity::getId, id)
                .eq(ArticleEntity::getUserId, userId));
        if (entity == null) {
            throw new IllegalArgumentException("文章不存在或无权访问");
        }
        return entity;
    }

    private String normalizeStatus(String status, boolean withDefault) {
        if (!StringUtils.hasText(status)) {
            return withDefault ? DEFAULT_STATUS : null;
        }
        String normalized = status.trim();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("文章状态不合法");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String toPlainText(String markdown) {
        String text = markdown;
        text = text.replaceAll("(?s)```.*?```", " ");
        text = text.replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", "");
        text = text.replaceAll("!\\[([^]]*)]\\([^)]*\\)", "$1");
        text = text.replaceAll("\\[([^]]+)]\\([^)]*\\)", "$1");
        text = text.replaceAll("(?m)^\\s{0,3}>\\s?", "");
        text = text.replaceAll("(?m)^\\s*[-*+]\\s+", "");
        text = text.replaceAll("(?m)^\\s*\\d+\\.\\s+", "");
        text = text.replaceAll("[*_`~#>|-]", " ");
        text = HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
        text = text.replaceAll("&nbsp;", " ");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        text = text.replaceAll("&amp;", "&");
        return text.replaceAll("\\s+", " ").trim();
    }

    private int countWords(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return 0;
        }
        int count = 0;
        boolean inAsciiWord = false;
        for (int i = 0; i < plainText.length(); i++) {
            char ch = plainText.charAt(i);
            if (Character.isWhitespace(ch)) {
                inAsciiWord = false;
            } else if (isAsciiLetterOrDigit(ch)) {
                if (!inAsciiWord) {
                    count++;
                    inAsciiWord = true;
                }
            } else {
                count++;
                inAsciiWord = false;
            }
        }
        return count;
    }

    private boolean isAsciiLetterOrDigit(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9');
    }

    private long normalizePage(Integer page) {
        return page == null || page < 1 ? 1L : page.longValue();
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 50L;
        }
        return Math.min(pageSize, 200);
    }
}
