package top.aiolife.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.pojo.entity.ArticleEntity;
import top.aiolife.record.pojo.req.ArticleQueryReq;
import top.aiolife.record.pojo.req.ArticleSaveReq;
import top.aiolife.record.pojo.vo.ArticleDetailVO;
import top.aiolife.record.pojo.vo.ArticleListVO;

/**
 * 文章服务接口，提供文章查询、详情、新增、编辑和删除能力。
 *
 * @author Ethan
 * @date 2026-06-24
 */
public interface IArticleService extends IService<ArticleEntity> {

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
    PageResp<ArticleListVO> query(ArticleQueryReq req, Long userId);

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
    ArticleDetailVO detail(Long id, Long userId);

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
    ArticleDetailVO create(ArticleSaveReq req, Long userId);

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
    ArticleDetailVO update(ArticleSaveReq req, Long userId);

    /**
     * 逻辑删除当前用户的文章。
     *
     * @param id 文章 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-06-24
     */
    void delete(Long id, Long userId);
}
