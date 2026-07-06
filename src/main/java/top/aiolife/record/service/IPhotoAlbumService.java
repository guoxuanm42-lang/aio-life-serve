package top.aiolife.record.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import top.aiolife.core.resq.PageResp;
import top.aiolife.record.pojo.entity.PhotoFolderEntity;
import top.aiolife.record.pojo.entity.PhotoImageEntity;
import top.aiolife.record.pojo.req.PhotoFolderCreateReq;
import top.aiolife.record.pojo.req.PhotoFolderUpdateReq;
import top.aiolife.record.pojo.req.PhotoImageMoveReq;
import top.aiolife.record.pojo.req.PhotoImageQueryReq;
import top.aiolife.record.pojo.req.PhotoImageUpdateReq;
import top.aiolife.record.pojo.vo.PhotoFolderTreeVO;
import top.aiolife.record.pojo.vo.PhotoImageUploadResultVO;

import java.util.List;

/**
 * 相册服务，提供文件夹管理、图片批量上传、图片查询和权限校验预览能力。
 *
 * @author Ethan
 * @date 2026-07-02
 */
public interface IPhotoAlbumService {

    /**
     * 查询当前用户的文件夹树。
     *
     * @param userId 当前用户 ID
     * @return 文件夹树
     *
     * @author Ethan
     * @date 2026-07-02
     */
    List<PhotoFolderTreeVO> listFolderTree(Long userId);

    /**
     * 创建相册文件夹。
     *
     * @param req 创建请求
     * @param userId 当前用户 ID
     * @return 创建后的文件夹
     *
     * @author Ethan
     * @date 2026-07-02
     */
    PhotoFolderEntity createFolder(PhotoFolderCreateReq req, Long userId);

    /**
     * 更新相册文件夹名称。
     *
     * @param req 更新请求
     * @param userId 当前用户 ID
     * @return 更新后的文件夹
     *
     * @author Ethan
     * @date 2026-07-02
     */
    PhotoFolderEntity updateFolder(PhotoFolderUpdateReq req, Long userId);

    /**
     * 删除空相册文件夹。
     *
     * @param id 文件夹 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-07-02
     */
    void deleteFolder(Long id, Long userId);

    /**
     * 批量上传图片到指定相册文件夹。
     *
     * @param folderId 文件夹 ID
     * @param files 图片文件列表
     * @param userId 当前用户 ID
     * @return 批量上传结果
     *
     * @author Ethan
     * @date 2026-07-02
     */
    PhotoImageUploadResultVO uploadImages(Long folderId, MultipartFile[] files, Long userId);

    /**
     * 分页查询指定文件夹下的图片。
     *
     * @param req 查询请求
     * @param userId 当前用户 ID
     * @return 图片分页数据
     *
     * @author Ethan
     * @date 2026-07-02
     */
    PageResp<PhotoImageEntity> queryImages(PhotoImageQueryReq req, Long userId);

    PhotoImageEntity updateImage(PhotoImageUpdateReq req, Long userId);

    PhotoImageEntity moveImage(PhotoImageMoveReq req, Long userId);

    void deleteImage(Long id, Long userId);

    PhotoFolderEntity setFolderCover(Long folderId, Long imageId, Long userId);

    PhotoFolderEntity clearFolderCover(Long folderId, Long userId);

    /**
     * 预览当前用户拥有的图片。
     *
     * @param id 图片 ID
     * @param userId 当前用户 ID
     * @param response Http 响应对象
     *
     * @author Ethan
     * @date 2026-07-02
     */
    void previewImage(Long id, Long userId, HttpServletResponse response);
}
