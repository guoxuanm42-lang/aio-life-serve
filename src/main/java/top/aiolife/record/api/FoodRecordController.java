package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.req.FoodRecordQueryReq;
import top.aiolife.record.pojo.req.FoodRecordSaveReq;
import top.aiolife.record.pojo.vo.FoodRecordDetailVO;
import top.aiolife.record.service.IFoodRecordService;

/**
 * 美食记录控制器，提供做饭记录的新增、编辑、删除、查询和详情接口。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/food-record")
public class FoodRecordController {

    private final IFoodRecordService foodRecordService;

    /**
     * 分页查询当前用户的美食记录。
     *
     * <p>用途：前端按菜名、分类、餐次、状态、日期范围和标签筛选美食记录列表。</p>
     *
     * @param req 美食记录查询请求
     * @return 统一返回结构，data.items 为美食记录列表，data.total 为总数
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @PostMapping("/query")
    public ApiResponse<PageResp<FoodRecordEntity>> query(@RequestBody(required = false) FoodRecordQueryReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(foodRecordService.query(req, userId));
    }

    /**
     * 查询当前用户的美食记录详情。
     *
     * <p>用途：前端打开详情页时获取主记录、材料清单和步骤流程。</p>
     *
     * @param id 美食记录 ID
     * @return 统一返回结构，data 为美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @GetMapping("/detail")
    public ApiResponse<FoodRecordDetailVO> detail(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(foodRecordService.detail(id, userId));
    }

    /**
     * 新增美食记录。
     *
     * <p>用途：前端提交菜名、基础信息、材料清单、步骤流程和复盘内容，后端创建当前用户的美食记录。</p>
     *
     * @param req 美食记录保存请求
     * @return 统一返回结构，data 为创建后的美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @PostMapping("/save")
    public ApiResponse<FoodRecordDetailVO> save(@RequestBody FoodRecordSaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(foodRecordService.create(req, userId));
    }

    /**
     * 编辑美食记录。
     *
     * <p>用途：前端保存已存在美食记录的主记录、材料清单和步骤流程，后端按当前用户校验归属后更新。</p>
     *
     * @param req 美食记录保存请求，必须包含 id
     * @return 统一返回结构，data 为更新后的美食记录详情
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @PostMapping("/update")
    public ApiResponse<FoodRecordDetailVO> update(@RequestBody FoodRecordSaveReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(foodRecordService.update(req, userId));
    }

    /**
     * 删除美食记录。
     *
     * <p>用途：前端删除当前用户的美食记录，后端对主记录、材料和步骤执行逻辑删除。</p>
     *
     * @param entity 删除请求，只需要 id
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody FoodRecordEntity entity) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = entity == null ? null : entity.getId();
        foodRecordService.delete(id, userId);
        return ApiResponse.success();
    }
}
