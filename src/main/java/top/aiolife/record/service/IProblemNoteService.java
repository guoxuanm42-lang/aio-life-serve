package top.aiolife.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;
import top.aiolife.record.pojo.req.ProblemNoteQueryReq;
import top.aiolife.record.pojo.req.ProblemNoteSaveReq;

/**
 * 题目记录服务接口，提供题目记录的查询、详情、新增、编辑和删除能力。
 *
 * @author Ethan
 * @date 2026-06-22
 */
public interface IProblemNoteService extends IService<ProblemNoteEntity> {

    /**
     * 分页查询当前用户的题目记录。
     *
     * @param req 查询请求
     * @param userId 当前用户 ID
     * @return 题目记录分页数据
     *
     * @author Ethan
     * @date 2026-06-22
     */
    PageResp<ProblemNoteEntity> query(ProblemNoteQueryReq req, Long userId);

    /**
     * 查询当前用户的题目记录详情。
     *
     * @param id 题目记录 ID
     * @param userId 当前用户 ID
     * @return 题目记录详情
     *
     * @author Ethan
     * @date 2026-06-22
     */
    ProblemNoteEntity detail(Long id, Long userId);

    /**
     * 新增题目记录。
     *
     * @param req 保存请求
     * @param userId 当前用户 ID
     * @return 新增后的题目记录
     *
     * @author Ethan
     * @date 2026-06-22
     */
    ProblemNoteEntity create(ProblemNoteSaveReq req, Long userId);

    /**
     * 更新题目记录。
     *
     * @param req 保存请求，必须包含 ID
     * @param userId 当前用户 ID
     * @return 更新后的题目记录
     *
     * @author Ethan
     * @date 2026-06-22
     */
    ProblemNoteEntity update(ProblemNoteSaveReq req, Long userId);

    /**
     * 逻辑删除题目记录。
     *
     * @param id 题目记录 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-06-22
     */
    void delete(Long id, Long userId);
}
