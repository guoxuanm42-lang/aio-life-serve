package top.aiolife.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.record.pojo.entity.ProblemCategoryEntity;
import top.aiolife.record.pojo.req.ProblemCategorySaveReq;
import top.aiolife.record.pojo.vo.ProblemCategoryListVO;

/**
 * 题目分类服务接口，提供分类列表、新增、编辑和删除能力。
 *
 * @author Ethan
 * @date 2026-06-22
 */
public interface IProblemCategoryService extends IService<ProblemCategoryEntity> {

    /**
     * 查询当前用户的题目分类列表和题目数量。
     *
     * @param userId 当前用户 ID
     * @return 分类列表展示对象
     *
     * @author Ethan
     * @date 2026-06-22
     */
    ProblemCategoryListVO listWithCount(Long userId);

    /**
     * 新增题目分类。
     *
     * @param req 保存请求
     * @param userId 当前用户 ID
     * @return 新增后的题目分类
     *
     * @author Ethan
     * @date 2026-06-22
     */
    ProblemCategoryEntity create(ProblemCategorySaveReq req, Long userId);

    /**
     * 更新题目分类。
     *
     * @param req 保存请求，必须包含 ID
     * @param userId 当前用户 ID
     * @return 更新后的题目分类
     *
     * @author Ethan
     * @date 2026-06-22
     */
    ProblemCategoryEntity update(ProblemCategorySaveReq req, Long userId);

    /**
     * 删除题目分类，并将该分类下题目改为未分类。
     *
     * @param id 分类 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-06-22
     */
    void delete(Long id, Long userId);
}
