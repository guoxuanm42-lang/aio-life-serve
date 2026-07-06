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
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.pojo.entity.PhotoFolderEntity;
import top.aiolife.record.pojo.entity.PhotoImageEntity;
import top.aiolife.record.pojo.req.PhotoFolderCreateReq;
import top.aiolife.record.pojo.req.PhotoFolderCoverReq;
import top.aiolife.record.pojo.req.PhotoFolderDeleteReq;
import top.aiolife.record.pojo.req.PhotoFolderUpdateReq;
import top.aiolife.record.pojo.req.PhotoImageDeleteReq;
import top.aiolife.record.pojo.req.PhotoImageMoveReq;
import top.aiolife.record.pojo.req.PhotoImageQueryReq;
import top.aiolife.record.pojo.req.PhotoImageUpdateReq;
import top.aiolife.record.pojo.vo.PhotoFolderTreeVO;
import top.aiolife.record.pojo.vo.PhotoImageUploadResultVO;
import top.aiolife.record.service.IPhotoAlbumService;

import java.util.List;

/**
 * 相册控制器，提供文件夹管理、图片批量上传、图片列表查询和权限校验预览接口。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/photo-album")
public class PhotoAlbumController {

    private final IPhotoAlbumService photoAlbumService;

    /**
     * 查询相册文件夹树接口。
     *
     * <p>用途：前端进入相册页时获取当前用户的多级文件夹结构。</p>
     *
     * @return 统一返回结构，data 为文件夹树列表
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @GetMapping("/folder/tree")
    public ApiResponse<List<PhotoFolderTreeVO>> listFolderTree() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(photoAlbumService.listFolderTree(userId));
    }

    /**
     * 创建相册文件夹接口。
     *
     * <p>用途：前端在根目录或指定父文件夹下创建新文件夹。</p>
     *
     * @param req 创建文件夹请求体，包含父文件夹 ID 和文件夹名称
     * @return 统一返回结构，data 为创建后的文件夹
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @PostMapping("/folder/create")
    public ApiResponse<PhotoFolderEntity> createFolder(@RequestBody PhotoFolderCreateReq req) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(photoAlbumService.createFolder(req, userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("创建相册文件夹失败", e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "创建失败");
        }
    }

    /**
     * 更新相册文件夹接口。
     *
     * <p>用途：前端修改当前用户拥有的文件夹名称。</p>
     *
     * @param req 更新文件夹请求体，包含文件夹 ID 和新名称
     * @return 统一返回结构，data 为更新后的文件夹
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @PostMapping("/folder/update")
    public ApiResponse<PhotoFolderEntity> updateFolder(@RequestBody PhotoFolderUpdateReq req) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(photoAlbumService.updateFolder(req, userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("更新相册文件夹失败", e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "更新失败");
        }
    }

    /**
     * 删除空相册文件夹接口。
     *
     * <p>用途：前端删除当前用户拥有的空文件夹；包含子文件夹或图片时后端拒绝删除。</p>
     *
     * @param req 删除文件夹请求体，包含文件夹 ID
     * @return 统一返回结构，data 为空
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @PostMapping("/folder/delete")
    public ApiResponse<Void> deleteFolder(@RequestBody PhotoFolderDeleteReq req) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Long id = req == null ? null : req.getId();
            photoAlbumService.deleteFolder(id, userId);
            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("删除相册文件夹失败", e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "删除失败");
        }
    }

    /**
     * 批量上传相册图片接口。
     *
     * <p>用途：前端在指定文件夹中一次选择多张图片，后端逐张写入 MinIO 并保存图片元数据。</p>
     *
     * @param folderId 文件夹 ID
     * @param files 图片文件数组，multipart/form-data 字段名为 files
     * @return 统一返回结构，data.successList 为成功图片，data.failureList 为失败文件
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @PostMapping("/image/upload-batch")
    public ApiResponse<PhotoImageUploadResultVO> uploadImages(@RequestParam("folderId") Long folderId,
                                                              @RequestParam("files") MultipartFile[] files) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(photoAlbumService.uploadImages(folderId, files, userId));
    }

    /**
     * 查询相册图片列表接口。
     *
     * <p>用途：前端按文件夹分页加载当前用户的图片，默认按上传时间倒序展示。</p>
     *
     * @param req 图片查询请求体，包含文件夹 ID、页码和每页数量
     * @return 统一返回结构，data.items 为图片列表，data.total 为总数
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @PostMapping("/image/query")
    public ApiResponse<PageResp<PhotoImageEntity>> queryImages(@RequestBody PhotoImageQueryReq req) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(photoAlbumService.queryImages(req, userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("查询相册图片失败", e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "查询失败");
        }
    }

    /**
     * 预览相册图片接口。
     *
     * <p>用途：前端展示缩略图或全屏预览时，后端先校验当前用户拥有该图片，再从 MinIO 输出图片流。</p>
     *
     * @param id 图片 ID
     * @param response Http 响应对象，返回图片文件流
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @PostMapping("/image/update")
    public ApiResponse<PhotoImageEntity> updateImage(@RequestBody PhotoImageUpdateReq req) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(photoAlbumService.updateImage(req, userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("更新相册图片失败", e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "更新失败");
        }
    }

    @PostMapping("/image/move")
    public ApiResponse<PhotoImageEntity> moveImage(@RequestBody PhotoImageMoveReq req) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(photoAlbumService.moveImage(req, userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("移动相册图片失败", e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "移动失败");
        }
    }

    @PostMapping("/image/delete")
    public ApiResponse<Void> deleteImage(@RequestBody PhotoImageDeleteReq req) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Long id = req == null ? null : req.getId();
            photoAlbumService.deleteImage(id, userId);
            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("删除相册图片失败", e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "删除失败");
        }
    }

    @PostMapping("/folder/cover/set")
    public ApiResponse<PhotoFolderEntity> setFolderCover(@RequestBody PhotoFolderCoverReq req) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Long folderId = req == null ? null : req.getFolderId();
            Long imageId = req == null ? null : req.getImageId();
            return ApiResponse.success(photoAlbumService.setFolderCover(folderId, imageId, userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("设置相册文件夹封面失败", e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "设置失败");
        }
    }

    @PostMapping("/folder/cover/clear")
    public ApiResponse<PhotoFolderEntity> clearFolderCover(@RequestBody PhotoFolderCoverReq req) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Long folderId = req == null ? null : req.getFolderId();
            return ApiResponse.success(photoAlbumService.clearFolderCover(folderId, userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("清除相册文件夹封面失败", e);
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "清除失败");
        }
    }

    @GetMapping("/image/preview")
    public void previewImage(@RequestParam("id") Long id, HttpServletResponse response) {
        Long userId = StpUtil.getLoginIdAsLong();
        photoAlbumService.previewImage(id, userId, response);
    }
}
