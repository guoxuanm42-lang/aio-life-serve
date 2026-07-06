package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.aiolife.config.MinioConfig;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.core.resq.PageResp;
import top.aiolife.core.util.MinioUtil;
import top.aiolife.record.mapper.IPhotoFolderMapper;
import top.aiolife.record.mapper.IPhotoImageMapper;
import top.aiolife.record.pojo.entity.PhotoFolderEntity;
import top.aiolife.record.pojo.entity.PhotoImageEntity;
import top.aiolife.record.pojo.req.PhotoFolderCreateReq;
import top.aiolife.record.pojo.req.PhotoFolderUpdateReq;
import top.aiolife.record.pojo.req.PhotoImageMoveReq;
import top.aiolife.record.pojo.req.PhotoImageQueryReq;
import top.aiolife.record.pojo.req.PhotoImageUpdateReq;
import top.aiolife.record.pojo.vo.PhotoFolderTreeVO;
import top.aiolife.record.pojo.vo.PhotoImageUploadResultVO;
import top.aiolife.record.service.IPhotoAlbumService;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 相册服务实现，基于数据库管理文件夹和图片元数据，并复用 MinIO 保存图片文件。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoAlbumServiceImpl implements IPhotoAlbumService {

    private static final Long ROOT_PARENT_ID = 0L;

    private static final String DEFAULT_BUCKET_NAME = "aiolife";

    private static final DateTimeFormatter OBJECT_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/gif", "image/jpeg", "image/png", "image/webp");

    private final IPhotoFolderMapper photoFolderMapper;

    private final IPhotoImageMapper photoImageMapper;

    private final MinioUtil minioUtil;

    private final MinioConfig minioConfig;

    /**
     * 查询当前用户的文件夹树。
     *
     * @param userId 当前用户 ID
     * @return 文件夹树
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @Override
    public List<PhotoFolderTreeVO> listFolderTree(Long userId) {
        LambdaQueryWrapper<PhotoFolderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.eq(PhotoFolderEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.orderByAsc(PhotoFolderEntity::getSortOrder);
        wrapper.orderByAsc(PhotoFolderEntity::getCreateTime);
        List<PhotoFolderEntity> folders = photoFolderMapper.selectList(wrapper);
        Map<Long, PhotoFolderTreeVO> nodeMap = folders.stream()
                .map(this::toTreeNode)
                .collect(Collectors.toMap(PhotoFolderTreeVO::getId, Function.identity()));
        List<PhotoFolderTreeVO> roots = new ArrayList<>();
        for (PhotoFolderEntity folder : folders) {
            PhotoFolderTreeVO node = nodeMap.get(folder.getId());
            Long parentId = normalizeParentId(folder.getParentId());
            PhotoFolderTreeVO parent = nodeMap.get(parentId);
            if (ROOT_PARENT_ID.equals(parentId) || parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        sortTree(roots);
        fillFolderStats(roots, userId);
        return roots;
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoFolderEntity createFolder(PhotoFolderCreateReq req, Long userId) {
        if (req == null) {
            throw new IllegalArgumentException("文件夹信息不能为空");
        }
        Long parentId = normalizeParentId(req.getParentId());
        if (!ROOT_PARENT_ID.equals(parentId)) {
            getOwnedFolder(parentId, userId);
        }
        PhotoFolderEntity entity = new PhotoFolderEntity();
        entity.setUserId(userId);
        entity.setParentId(parentId);
        entity.setName(normalizeRequired(req.getName(), "文件夹名称不能为空"));
        entity.setSortOrder(nextFolderSortOrder(parentId, userId));
        entity.fillCreateCommonField(userId);
        photoFolderMapper.insert(entity);
        return entity;
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoFolderEntity updateFolder(PhotoFolderUpdateReq req, Long userId) {
        if (req == null || req.getId() == null) {
            throw new IllegalArgumentException("文件夹 ID 不能为空");
        }
        PhotoFolderEntity exist = getOwnedFolder(req.getId(), userId);
        PhotoFolderEntity update = new PhotoFolderEntity();
        update.setId(exist.getId());
        update.setName(normalizeRequired(req.getName(), "文件夹名称不能为空"));
        update.fillUpdateCommonField(userId);
        photoFolderMapper.updateById(update);
        return getOwnedFolder(req.getId(), userId);
    }

    /**
     * 删除空相册文件夹。
     *
     * @param id 文件夹 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long id, Long userId) {
        PhotoFolderEntity folder = getOwnedFolder(id, userId);
        if (countChildFolders(id, userId) > 0) {
            throw new IllegalArgumentException("文件夹下存在子文件夹，不能删除");
        }
        if (countImages(id, userId) > 0) {
            throw new IllegalArgumentException("文件夹下存在图片，不能删除");
        }
        folder.setIsDeleted(StatusConst.IS_DELETE);
        folder.fillUpdateCommonField(userId);
        photoFolderMapper.updateById(folder);
    }

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
    @Override
    public PhotoImageUploadResultVO uploadImages(Long folderId, MultipartFile[] files, Long userId) {
        PhotoImageUploadResultVO result = new PhotoImageUploadResultVO();
        try {
            getOwnedFolder(folderId, userId);
        } catch (Exception e) {
            addFailure(result, null, e.getMessage());
            return result;
        }
        if (files == null || files.length == 0) {
            addFailure(result, null, "请选择图片文件");
            return result;
        }
        for (MultipartFile file : files) {
            uploadSingleImage(folderId, file, userId, result);
        }
        return result;
    }

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
    @Override
    public PageResp<PhotoImageEntity> queryImages(PhotoImageQueryReq req, Long userId) {
        if (req == null || req.getFolderId() == null) {
            throw new IllegalArgumentException("文件夹 ID 不能为空");
        }
        getOwnedFolder(req.getFolderId(), userId);
        LambdaQueryWrapper<PhotoImageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoImageEntity::getUserId, userId);
        List<Long> folderIds = listDescendantFolderIds(req.getFolderId(), userId);
        wrapper.in(PhotoImageEntity::getFolderId, folderIds);
        wrapper.eq(PhotoImageEntity::getIsDeleted, StatusConst.NO_DELETE);
        if (StringUtils.hasText(req.getKeyword())) {
            String keyword = req.getKeyword().trim();
            wrapper.and(item -> item.like(PhotoImageEntity::getTitle, keyword)
                    .or()
                    .like(PhotoImageEntity::getCaption, keyword)
                    .or()
                    .like(PhotoImageEntity::getOriginalFilename, keyword));
        }
        wrapper.orderByDesc(PhotoImageEntity::getCreateTime);
        Page<PhotoImageEntity> page = new Page<>(normalizePage(req.getPage()), normalizePageSize(req.getPageSize()));
        IPage<PhotoImageEntity> iPage = photoImageMapper.selectPage(page, wrapper);
        return PageResp.of(iPage.getRecords(), iPage.getTotal());
    }

    /**
     * 更新图片标题和备注。
     *
     * @param req 更新请求
     * @param userId 当前用户 ID
     * @return 更新后的图片
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoImageEntity updateImage(PhotoImageUpdateReq req, Long userId) {
        if (req == null || req.getId() == null) {
            throw new IllegalArgumentException("图片 ID 不能为空");
        }
        PhotoImageEntity exist = getOwnedImage(req.getId(), userId);
        LambdaUpdateWrapper<PhotoImageEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PhotoImageEntity::getId, exist.getId());
        wrapper.eq(PhotoImageEntity::getUserId, userId);
        wrapper.set(PhotoImageEntity::getTitle, normalizeBlank(req.getTitle()));
        wrapper.set(PhotoImageEntity::getCaption, normalizeBlank(req.getCaption()));
        wrapper.set(PhotoImageEntity::getUpdateUser, userId);
        wrapper.set(PhotoImageEntity::getUpdateTime, java.time.LocalDateTime.now());
        photoImageMapper.update(null, wrapper);
        return getOwnedImage(exist.getId(), userId);
    }

    /**
     * 移动图片到目标文件夹。
     *
     * @param req 移动请求
     * @param userId 当前用户 ID
     * @return 移动后的图片
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoImageEntity moveImage(PhotoImageMoveReq req, Long userId) {
        if (req == null || req.getId() == null || req.getTargetFolderId() == null) {
            throw new IllegalArgumentException("图片 ID 和目标文件夹 ID 不能为空");
        }
        PhotoImageEntity exist = getOwnedImage(req.getId(), userId);
        getOwnedFolder(req.getTargetFolderId(), userId);
        PhotoImageEntity update = new PhotoImageEntity();
        update.setId(exist.getId());
        update.setFolderId(req.getTargetFolderId());
        update.fillUpdateCommonField(userId);
        photoImageMapper.updateById(update);
        clearInvalidCoversAfterMove(exist.getId(), req.getTargetFolderId(), userId);
        return getOwnedImage(exist.getId(), userId);
    }

    /**
     * 逻辑删除图片。
     *
     * @param id 图片 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(Long id, Long userId) {
        PhotoImageEntity exist = getOwnedImage(id, userId);
        exist.setIsDeleted(StatusConst.IS_DELETE);
        exist.fillUpdateCommonField(userId);
        photoImageMapper.updateById(exist);
        clearCoversByImage(id, userId);
    }

    /**
     * 设置文件夹封面。
     *
     * @param folderId 文件夹 ID
     * @param imageId 图片 ID
     * @param userId 当前用户 ID
     * @return 更新后的文件夹
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoFolderEntity setFolderCover(Long folderId, Long imageId, Long userId) {
        PhotoFolderEntity folder = getOwnedFolder(folderId, userId);
        PhotoImageEntity image = getOwnedImage(imageId, userId);
        if (!listDescendantFolderIds(folderId, userId).contains(image.getFolderId())) {
            throw new IllegalArgumentException("只能选择当前文件夹或子文件夹内的图片作为封面");
        }
        PhotoFolderEntity update = new PhotoFolderEntity();
        update.setId(folder.getId());
        update.setCoverImageId(image.getId());
        update.fillUpdateCommonField(userId);
        photoFolderMapper.updateById(update);
        return getOwnedFolder(folderId, userId);
    }

    /**
     * 清除文件夹手动封面。
     *
     * @param folderId 文件夹 ID
     * @param userId 当前用户 ID
     * @return 更新后的文件夹
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoFolderEntity clearFolderCover(Long folderId, Long userId) {
        getOwnedFolder(folderId, userId);
        LambdaUpdateWrapper<PhotoFolderEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PhotoFolderEntity::getId, folderId);
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.set(PhotoFolderEntity::getCoverImageId, null);
        wrapper.set(PhotoFolderEntity::getUpdateUser, userId);
        wrapper.set(PhotoFolderEntity::getUpdateTime, java.time.LocalDateTime.now());
        photoFolderMapper.update(null, wrapper);
        return getOwnedFolder(folderId, userId);
    }

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
    @Override
    public void previewImage(Long id, Long userId, HttpServletResponse response) {
        PhotoImageEntity image;
        try {
            image = getOwnedImage(id, userId);
            response.setContentType(resolveContentType(image));
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
            log.error("预览相册图片失败: id={}", id, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void uploadSingleImage(Long folderId, MultipartFile file, Long userId, PhotoImageUploadResultVO result) {
        String filename = file == null ? null : file.getOriginalFilename();
        try {
            validateImageFile(file);
            String contentType = file.getContentType();
            String ext = detectExt(filename, contentType);
            String bucketName = StringUtils.hasText(minioConfig.getBucketName()) ? minioConfig.getBucketName() : DEFAULT_BUCKET_NAME;
            String objectKey = "photo-album/" + userId + "/" + folderId + "/" + LocalDate.now().format(OBJECT_DATE_FORMATTER) + "/"
                    + UUID.randomUUID().toString().replace("-", "") + "." + ext;
            minioUtil.uploadFile(bucketName, file, objectKey);

            PhotoImageEntity entity = new PhotoImageEntity();
            entity.setUserId(userId);
            entity.setFolderId(folderId);
            entity.setBucketName(bucketName);
            entity.setObjectKey(objectKey);
            entity.setOriginalFilename(filename);
            entity.setContentType(contentType);
            entity.setFileSize(file.getSize());
            entity.setSortOrder(nextImageSortOrder(folderId, userId));
            entity.fillCreateCommonField(userId);
            photoImageMapper.insert(entity);
            result.getSuccessList().add(entity);
        } catch (Exception e) {
            log.warn("上传相册图片失败: filename={}", filename, e);
            addFailure(result, filename, e.getMessage());
        }
    }

    private PhotoFolderEntity getOwnedFolder(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("文件夹 ID 不能为空");
        }
        LambdaQueryWrapper<PhotoFolderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoFolderEntity::getId, id);
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.eq(PhotoFolderEntity::getIsDeleted, StatusConst.NO_DELETE);
        PhotoFolderEntity folder = photoFolderMapper.selectOne(wrapper);
        if (folder == null) {
            throw new IllegalArgumentException("文件夹不存在或无权操作");
        }
        return folder;
    }

    private PhotoImageEntity getOwnedImage(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("图片 ID 不能为空");
        }
        LambdaQueryWrapper<PhotoImageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoImageEntity::getId, id);
        wrapper.eq(PhotoImageEntity::getUserId, userId);
        wrapper.eq(PhotoImageEntity::getIsDeleted, StatusConst.NO_DELETE);
        PhotoImageEntity image = photoImageMapper.selectOne(wrapper);
        if (image == null) {
            throw new IllegalArgumentException("图片不存在或无权操作");
        }
        getOwnedFolder(image.getFolderId(), userId);
        return image;
    }

    private int countChildFolders(Long parentId, Long userId) {
        LambdaQueryWrapper<PhotoFolderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoFolderEntity::getParentId, parentId);
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.eq(PhotoFolderEntity::getIsDeleted, StatusConst.NO_DELETE);
        return photoFolderMapper.selectCount(wrapper).intValue();
    }

    private int countImages(Long folderId, Long userId) {
        LambdaQueryWrapper<PhotoImageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoImageEntity::getFolderId, folderId);
        wrapper.eq(PhotoImageEntity::getUserId, userId);
        wrapper.eq(PhotoImageEntity::getIsDeleted, StatusConst.NO_DELETE);
        return photoImageMapper.selectCount(wrapper).intValue();
    }

    private void fillFolderStats(List<PhotoFolderTreeVO> nodes, Long userId) {
        for (PhotoFolderTreeVO node : nodes) {
            List<Long> folderIds = collectTreeFolderIds(node);
            node.setImageCount(countImages(folderIds, userId));
            PhotoImageEntity cover = resolveCoverImage(node.getId(), node.getCoverImageId(), folderIds, userId);
            if (cover != null) {
                node.setCoverImageId(cover.getId());
                node.setCoverImageUrl("/photo-album/image/preview?id=" + cover.getId());
            } else {
                node.setCoverImageId(null);
                node.setCoverImageUrl(null);
            }
            fillFolderStats(node.getChildren(), userId);
        }
    }

    private List<Long> collectTreeFolderIds(PhotoFolderTreeVO node) {
        List<Long> ids = new ArrayList<>();
        ids.add(node.getId());
        for (PhotoFolderTreeVO child : node.getChildren()) {
            ids.addAll(collectTreeFolderIds(child));
        }
        return ids;
    }

    private List<Long> listDescendantFolderIds(Long folderId, Long userId) {
        getOwnedFolder(folderId, userId);
        List<Long> ids = new ArrayList<>();
        ids.add(folderId);
        collectChildFolderIds(folderId, userId, ids);
        return ids;
    }

    private void collectChildFolderIds(Long parentId, Long userId, List<Long> ids) {
        LambdaQueryWrapper<PhotoFolderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoFolderEntity::getParentId, parentId);
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.eq(PhotoFolderEntity::getIsDeleted, StatusConst.NO_DELETE);
        List<PhotoFolderEntity> children = photoFolderMapper.selectList(wrapper);
        for (PhotoFolderEntity child : children) {
            ids.add(child.getId());
            collectChildFolderIds(child.getId(), userId, ids);
        }
    }

    private Long countImages(List<Long> folderIds, Long userId) {
        if (folderIds == null || folderIds.isEmpty()) {
            return 0L;
        }
        LambdaQueryWrapper<PhotoImageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PhotoImageEntity::getFolderId, folderIds);
        wrapper.eq(PhotoImageEntity::getUserId, userId);
        wrapper.eq(PhotoImageEntity::getIsDeleted, StatusConst.NO_DELETE);
        return photoImageMapper.selectCount(wrapper);
    }

    private PhotoImageEntity resolveCoverImage(Long folderId, Long manualCoverImageId, List<Long> folderIds, Long userId) {
        if (manualCoverImageId != null) {
            PhotoImageEntity manual = getOwnedImageOrNull(manualCoverImageId, userId);
            if (manual != null && folderIds.contains(manual.getFolderId())) {
                return manual;
            }
            clearFolderCoverSilently(folderId, userId);
        }
        return findLatestImage(folderIds, userId);
    }

    private PhotoImageEntity findLatestImage(List<Long> folderIds, Long userId) {
        if (folderIds == null || folderIds.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<PhotoImageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PhotoImageEntity::getFolderId, folderIds);
        wrapper.eq(PhotoImageEntity::getUserId, userId);
        wrapper.eq(PhotoImageEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.orderByDesc(PhotoImageEntity::getCreateTime);
        wrapper.last("LIMIT 1");
        return photoImageMapper.selectOne(wrapper);
    }

    private PhotoImageEntity getOwnedImageOrNull(Long id, Long userId) {
        try {
            return getOwnedImage(id, userId);
        } catch (Exception e) {
            return null;
        }
    }

    private void clearCoversByImage(Long imageId, Long userId) {
        LambdaUpdateWrapper<PhotoFolderEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.eq(PhotoFolderEntity::getCoverImageId, imageId);
        wrapper.set(PhotoFolderEntity::getCoverImageId, null);
        wrapper.set(PhotoFolderEntity::getUpdateUser, userId);
        wrapper.set(PhotoFolderEntity::getUpdateTime, java.time.LocalDateTime.now());
        photoFolderMapper.update(null, wrapper);
    }

    private void clearInvalidCoversAfterMove(Long imageId, Long targetFolderId, Long userId) {
        LambdaQueryWrapper<PhotoFolderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.eq(PhotoFolderEntity::getCoverImageId, imageId);
        wrapper.eq(PhotoFolderEntity::getIsDeleted, StatusConst.NO_DELETE);
        List<PhotoFolderEntity> folders = photoFolderMapper.selectList(wrapper);
        for (PhotoFolderEntity folder : folders) {
            if (!listDescendantFolderIds(folder.getId(), userId).contains(targetFolderId)) {
                clearFolderCoverSilently(folder.getId(), userId);
            }
        }
    }

    private void clearFolderCoverSilently(Long folderId, Long userId) {
        LambdaUpdateWrapper<PhotoFolderEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PhotoFolderEntity::getId, folderId);
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.set(PhotoFolderEntity::getCoverImageId, null);
        wrapper.set(PhotoFolderEntity::getUpdateUser, userId);
        wrapper.set(PhotoFolderEntity::getUpdateTime, java.time.LocalDateTime.now());
        photoFolderMapper.update(null, wrapper);
    }

    private Integer nextFolderSortOrder(Long parentId, Long userId) {
        LambdaQueryWrapper<PhotoFolderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoFolderEntity::getParentId, parentId);
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.eq(PhotoFolderEntity::getIsDeleted, StatusConst.NO_DELETE);
        return Math.max(photoFolderMapper.selectCount(wrapper).intValue(), 0);
    }

    private Integer nextImageSortOrder(Long folderId, Long userId) {
        LambdaQueryWrapper<PhotoImageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoImageEntity::getFolderId, folderId);
        wrapper.eq(PhotoImageEntity::getUserId, userId);
        wrapper.eq(PhotoImageEntity::getIsDeleted, StatusConst.NO_DELETE);
        return Math.max(photoImageMapper.selectCount(wrapper).intValue(), 0);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("图片文件不能为空");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("仅支持 jpg、png、webp、gif 图片");
        }
    }

    private String detectExt(String filename, String contentType) {
        if (StringUtils.hasText(filename) && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1).trim().toLowerCase();
            if (Set.of("gif", "jpeg", "jpg", "png", "webp").contains(ext)) {
                return "jpeg".equals(ext) ? "jpg" : ext;
            }
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/gif".equalsIgnoreCase(contentType)) {
            return "gif";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }
        return "jpg";
    }

    private void addFailure(PhotoImageUploadResultVO result, String filename, String reason) {
        PhotoImageUploadResultVO.FailureItem item = new PhotoImageUploadResultVO.FailureItem();
        item.setFilename(StringUtils.hasText(filename) ? filename : "未知文件");
        item.setReason(StringUtils.hasText(reason) ? reason : "上传失败");
        result.getFailureList().add(item);
    }

    private PhotoFolderTreeVO toTreeNode(PhotoFolderEntity entity) {
        PhotoFolderTreeVO vo = new PhotoFolderTreeVO();
        vo.setId(entity.getId());
        vo.setParentId(normalizeParentId(entity.getParentId()));
        vo.setName(entity.getName());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCoverImageId(entity.getCoverImageId());
        return vo;
    }

    private void sortTree(List<PhotoFolderTreeVO> nodes) {
        nodes.sort(Comparator.comparing((PhotoFolderTreeVO item) -> item.getSortOrder() == null ? 0 : item.getSortOrder())
                .thenComparing(item -> item.getId() == null ? 0L : item.getId()));
        for (PhotoFolderTreeVO node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null || parentId < 0 ? ROOT_PARENT_ID : parentId;
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private long normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 60;
        }
        return Math.min(pageSize, 120);
    }

    private String resolveContentType(PhotoImageEntity image) {
        if (StringUtils.hasText(image.getContentType())) {
            return image.getContentType();
        }
        String lower = image.getObjectKey() == null ? "" : image.getObjectKey().toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }
}
