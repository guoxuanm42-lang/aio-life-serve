package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.core.util.MinioUtil;
import top.aiolife.record.pojo.entity.FoodRecordImageEntity;
import top.aiolife.record.pojo.req.FoodRecordImageSortReq;
import top.aiolife.record.pojo.req.FoodRecordImageUpdateReq;
import top.aiolife.record.service.IFoodRecordImageService;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 美食记录图片控制器，提供图片上传、预览、元数据更新、排序和逻辑删除接口。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/food-record/image")
public class FoodRecordImageController {

    private final IFoodRecordImageService foodRecordImageService;

    private final MinioUtil minioUtil;

    /**
     * 上传美食记录图片接口。
     *
     * <p>用途：前端为当前用户拥有的美食记录上传图片，后端写入 MinIO 并保存图片元数据。</p>
     *
     * @param recordId 美食记录 ID
     * @param imageType 图片类型
     * @param caption 图片说明
     * @param file 图片文件，multipart/form-data 字段名为 file
     * @return 统一返回结构，data 为已保存的图片记录
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @PostMapping("/upload")
    public ApiResponse<FoodRecordImageEntity> upload(@RequestParam("recordId") Long recordId,
                                                     @RequestParam(value = "imageType", required = false) String imageType,
                                                     @RequestParam(value = "caption", required = false) String caption,
                                                     @RequestParam("file") MultipartFile file) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(foodRecordImageService.upload(recordId, imageType, caption, file, userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("上传美食记录图片失败: recordId={}", recordId, e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "上传失败");
        }
    }

    /**
     * 更新美食记录图片元数据接口。
     *
     * <p>用途：前端修改图片类型、说明或排序值，后端按当前用户校验图片归属后更新。</p>
     *
     * @param req 图片更新请求
     * @return 统一返回结构，data 为更新后的图片记录
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @PostMapping("/update")
    public ApiResponse<FoodRecordImageEntity> update(@RequestBody FoodRecordImageUpdateReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(foodRecordImageService.updateImage(req, userId));
    }

    /**
     * 批量更新美食记录图片排序接口。
     *
     * <p>用途：前端拖拽或上移下移图片后批量提交排序值。</p>
     *
     * @param req 图片排序请求
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @PostMapping("/sort")
    public ApiResponse<Void> sort(@RequestBody FoodRecordImageSortReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        foodRecordImageService.sortImages(req, userId);
        return ApiResponse.success();
    }

    /**
     * 逻辑删除美食记录图片接口。
     *
     * <p>用途：前端删除图片时仅删除图片元数据，不物理删除 MinIO 对象。</p>
     *
     * @param entity 删除请求，必须包含图片 ID
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody FoodRecordImageEntity entity) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = entity == null ? null : entity.getId();
        foodRecordImageService.deleteImage(id, userId);
        return ApiResponse.success();
    }

    /**
     * 预览美食记录图片接口。
     *
     * <p>用途：前端在详情和编辑页展示图片，后端先校验当前用户拥有该图片，再从 MinIO 输出图片流。</p>
     *
     * @param id 图片 ID
     * @param response Http 响应对象
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @GetMapping("/preview")
    public void preview(@RequestParam("id") Long id, HttpServletResponse response) {
        FoodRecordImageEntity image;
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            image = foodRecordImageService.getOwnedImage(id, userId);
            response.setContentType(resolveContentType(image.getObjectKey()));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try (InputStream inputStream = minioUtil.getFile(image.getBucketName(), image.getObjectKey());
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (Exception e) {
            log.error("预览美食记录图片失败: id={}", id, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String resolveContentType(String objectKey) {
        String lower = objectKey == null ? "" : objectKey.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }
        return "image/jpeg";
    }
}
