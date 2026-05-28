package top.aiolife.record.service;

import top.aiolife.core.resq.ApiResponse;
import top.aiolife.record.pojo.req.ThoughtSaveReq;

/**
 * 闪念（思考）服务
 *
 * @author Ethan
 */
public interface IThoughtService {

    ApiResponse<Boolean> save(ThoughtSaveReq req, long userId, String idempotencyKey);
}

