package top.aiolife.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import top.aiolife.record.pojo.entity.FoodRecordImageEntity;
import top.aiolife.record.pojo.req.FoodRecordImageSortReq;
import top.aiolife.record.pojo.req.FoodRecordImageUpdateReq;

import java.util.List;

/**
 * 美食记录图片服务，负责图片上传、查询、修改、排序和逻辑删除。
 *
 * @author Ethan
 * @date 2026-05-31
 */
public interface IFoodRecordImageService extends IService<FoodRecordImageEntity> {

    /**
     * 查询当前用户某条美食记录的图片列表。
     *
     * @param recordId 美食记录 ID
     * @param userId 当前用户 ID
     * @return 图片列表
     *
     * @author Ethan
     * @date 2026-05-31
     */
    List<FoodRecordImageEntity> listByRecord(Long recordId, Long userId);

    /**
     * 上传美食记录图片。
     *
     * @param recordId 美食记录 ID
     * @param imageType 图片类型
     * @param caption 图片说明
     * @param file 图片文件
     * @param userId 当前用户 ID
     * @return 已保存的图片记录
     * @throws Exception 上传失败时抛出
     *
     * @author Ethan
     * @date 2026-05-31
     */
    FoodRecordImageEntity upload(Long recordId, String imageType, String caption, MultipartFile file, Long userId) throws Exception;

    /**
     * 更新美食记录图片元数据。
     *
     * @param req 图片更新请求
     * @param userId 当前用户 ID
     * @return 更新后的图片记录
     *
     * @author Ethan
     * @date 2026-05-31
     */
    FoodRecordImageEntity updateImage(FoodRecordImageUpdateReq req, Long userId);

    /**
     * 批量更新美食记录图片排序。
     *
     * @param req 图片排序请求
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-05-31
     */
    void sortImages(FoodRecordImageSortReq req, Long userId);

    /**
     * 逻辑删除美食记录图片。
     *
     * @param id 图片 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-05-31
     */
    void deleteImage(Long id, Long userId);

    /**
     * 查询当前用户拥有的图片。
     *
     * @param id 图片 ID
     * @param userId 当前用户 ID
     * @return 图片记录
     *
     * @author Ethan
     * @date 2026-05-31
     */
    FoodRecordImageEntity getOwnedImage(Long id, Long userId);
}
