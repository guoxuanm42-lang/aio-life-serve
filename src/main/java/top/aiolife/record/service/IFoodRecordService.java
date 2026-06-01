package top.aiolife.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.req.FoodRecordQueryReq;
import top.aiolife.record.pojo.req.FoodRecordSaveReq;
import top.aiolife.record.pojo.vo.FoodRecordDetailVO;

/**
 * 美食记录服务接口，提供美食记录基础增删改查能力。
 *
 * @author Ethan
 * @date 2026-05-31
 */
public interface IFoodRecordService extends IService<FoodRecordEntity> {

    /**
     * 分页查询当前用户的美食记录。
     *
     * @param req 查询请求
     * @param userId 当前用户 ID
     * @return 分页美食记录
     *
     * @author Ethan
     * @date 2026-05-31
     */
    PageResp<FoodRecordEntity> query(FoodRecordQueryReq req, Long userId);

    /**
     * 查询当前用户的美食记录详情。
     *
     * @param id 美食记录 ID
     * @param userId 当前用户 ID
     * @return 美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    FoodRecordDetailVO detail(Long id, Long userId);

    /**
     * 新增美食记录。
     *
     * @param req 保存请求
     * @param userId 当前用户 ID
     * @return 美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    FoodRecordDetailVO create(FoodRecordSaveReq req, Long userId);

    /**
     * 更新美食记录。
     *
     * @param req 保存请求
     * @param userId 当前用户 ID
     * @return 美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    FoodRecordDetailVO update(FoodRecordSaveReq req, Long userId);

    /**
     * 逻辑删除美食记录及其材料和步骤。
     *
     * @param id 美食记录 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-05-31
     */
    void delete(Long id, Long userId);
}
