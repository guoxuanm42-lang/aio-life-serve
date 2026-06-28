package top.aiolife.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.record.pojo.entity.ArticleCategoryEntity;
import top.aiolife.record.pojo.req.ArticleCategorySaveReq;
import top.aiolife.record.pojo.vo.ArticleCategoryListVO;

/**
 * 文章分类服务接口，提供分类列表、新增、编辑和删除能力。
 *
 * @author Ethan
 * @date 2026-06-24
 */
public interface IArticleCategoryService extends IService<ArticleCategoryEntity> {

    /**
     * 查询当前用户的文章分类列表和文章数量。
     *
     * @param userId 当前用户 ID
     * @return 文章分类列表展示对象
     *
     * @author Ethan
     * @date 2026-06-24
     */
    ArticleCategoryListVO listWithCount(Long userId);

    /**
     * 新增当前用户的文章分类。
     *
     * @param req 分类保存请求
     * @param userId 当前用户 ID
     * @return 新增后的文章分类
     *
     * @author Ethan
     * @date 2026-06-24
     */
    ArticleCategoryEntity create(ArticleCategorySaveReq req, Long userId);

    /**
     * 更新当前用户的文章分类。
     *
     * @param req 分类保存请求，必须包含分类 ID
     * @param userId 当前用户 ID
     * @return 更新后的文章分类
     *
     * @author Ethan
     * @date 2026-06-24
     */
    ArticleCategoryEntity update(ArticleCategorySaveReq req, Long userId);

    /**
     * 删除当前用户的文章分类，并将该分类下文章转为未分类。
     *
     * @param id 分类 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-06-24
     */
    void delete(Long id, Long userId);
}
