package top.aiolife.record.service;

import top.aiolife.core.resq.ApiResponse;
import top.aiolife.record.pojo.req.CommonReq;
import top.aiolife.record.pojo.req.ThoughtSaveReq;
import top.aiolife.record.pojo.vo.ThoughtDetailVO;

/**
 * 闪念服务，负责主记录、事件流和结构化详情的读写。
 *
 * @author Ethan
 * @date 2026-06-12
 */
public interface IThoughtService {

    /**
     * 保存闪念主记录、事件流和当前类型结构化详情。
     *
     * @param req 闪念保存请求，包含主记录字段、事件流和当前类型详情
     * @param userId 当前登录用户 ID
     * @param idempotencyKey 幂等键，MCP 等外部调用可传入
     * @return 统一返回结构，data 表示是否保存成功
     *
     * @author Ethan
     * @date 2026-06-12
     */
    ApiResponse<Boolean> save(ThoughtSaveReq req, long userId, String idempotencyKey);

    /**
     * 更新闪念主记录、事件流和当前类型结构化详情。
     *
     * @param req 闪念更新请求，必须包含 id
     * @param userId 当前登录用户 ID
     * @return 统一返回结构，data 表示是否更新成功
     *
     * @author Ethan
     * @date 2026-06-12
     */
    ApiResponse<Boolean> update(ThoughtSaveReq req, long userId);

    /**
     * 查询闪念详情。
     *
     * @param id 闪念 ID
     * @param userId 当前登录用户 ID
     * @return 闪念详情，包含主记录、事件流和三类详情
     *
     * @author Ethan
     * @date 2026-06-12
     */
    ThoughtDetailVO detail(Long id, long userId);

    /**
     * 批量删除闪念及其关联扩展数据。
     *
     * @param req 批量删除请求，包含 idList
     * @param userId 当前登录用户 ID
     * @return 统一返回结构，data 表示是否删除成功
     *
     * @author Ethan
     * @date 2026-06-12
     */
    ApiResponse<Boolean> batchDelete(CommonReq req, long userId);
}
