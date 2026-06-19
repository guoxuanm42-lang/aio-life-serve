package top.aiolife.record.service;

import top.aiolife.record.pojo.req.ThoughtExportReq;

import java.io.OutputStream;

/**
 * 闪念数据导出服务。
 *
 * @author Ethan
 * @date 2026-06-13
 */
public interface IThoughtExportService {

    /**
     * 校验当前筛选范围是否允许同步导出。
     *
     * @param userId 当前登录用户 ID
     * @param req 导出筛选请求，支持分类、状态、类型和主题筛选
     *
     * @author Ethan
     * @date 2026-06-13
     */
    void validateExportable(long userId, ThoughtExportReq req);

    /**
     * 导出当前用户的闪念数据到 Excel 输出流。
     *
     * @param userId 当前登录用户 ID
     * @param req 导出筛选请求，支持分类、状态、类型和主题筛选
     * @param outputStream Excel 文件输出流
     *
     * @author Ethan
     * @date 2026-06-13
     */
    void exportThoughts(long userId, ThoughtExportReq req, OutputStream outputStream);
}
