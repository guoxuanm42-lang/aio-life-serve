package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.aiolife.config.MinioConfig;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.core.util.MinioUtil;
import top.aiolife.record.mapper.IFoodRecordImageMapper;
import top.aiolife.record.mapper.IFoodRecordMapper;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.entity.FoodRecordImageEntity;
import top.aiolife.record.pojo.req.FoodRecordImageSortReq;
import top.aiolife.record.pojo.req.FoodRecordImageUpdateReq;
import top.aiolife.record.service.IFoodRecordImageService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 美食记录图片服务实现，复用 MinIO 保存图片文件并按用户校验图片元数据归属。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Service
@RequiredArgsConstructor
public class FoodRecordImageServiceImpl extends ServiceImpl<IFoodRecordImageMapper, FoodRecordImageEntity> implements IFoodRecordImageService {

    private static final String DEFAULT_BUCKET_NAME = "aiolife";

    private static final String DEFAULT_IMAGE_TYPE = "other";

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("ingredient", "process", "finished", "failed", "other");

    private final IFoodRecordImageMapper foodRecordImageMapper;

    private final IFoodRecordMapper foodRecordMapper;

    private final MinioUtil minioUtil;

    private final MinioConfig minioConfig;

    /**
     * 查询当前用户某条美食记录的图片列表。
     *
     * @param recordId 美食记录 ID
     * @param userId 当前用户 ID
     * @return 图片列表
     *
     * @author Ethan
     * @date 2026-06-01
     */
    @Override
    public List<FoodRecordImageEntity> listByRecord(Long recordId, Long userId) {
        if (recordId == null || userId == null) {
            return List.of();
        }
        QueryWrapper<FoodRecordImageEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId);
        wrapper.eq("user_id", userId);
        wrapper.orderByAsc("image_type");
        wrapper.orderByAsc("sort_order");
        wrapper.orderByAsc("create_time");
        return foodRecordImageMapper.selectList(wrapper);
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodRecordImageEntity upload(Long recordId, String imageType, String caption, MultipartFile file, Long userId) throws Exception {
        ensureOwnedRecord(recordId, userId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("图片文件不能为空");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("仅支持图片文件");
        }
        String normalizedType = normalizeImageType(imageType, true);
        String ext = detectExt(file.getOriginalFilename(), contentType);
        String objectKey = "food-record/" + userId + "/" + recordId + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String bucketName = StringUtils.hasText(minioConfig.getBucketName()) ? minioConfig.getBucketName() : DEFAULT_BUCKET_NAME;
        minioUtil.uploadFile(bucketName, file, objectKey);

        FoodRecordImageEntity entity = new FoodRecordImageEntity();
        entity.setUserId(userId);
        entity.setRecordId(recordId);
        entity.setBucketName(bucketName);
        entity.setObjectKey(objectKey);
        entity.setImageType(normalizedType);
        entity.setCaption(normalizeBlank(caption));
        entity.setSortOrder(nextSortOrder(recordId, userId, normalizedType));
        entity.fillCreateCommonField(userId);
        foodRecordImageMapper.insert(entity);
        return entity;
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodRecordImageEntity updateImage(FoodRecordImageUpdateReq req, Long userId) {
        if (req == null || req.getId() == null) {
            throw new IllegalArgumentException("图片 ID 不能为空");
        }
        FoodRecordImageEntity exist = getOwnedImage(req.getId(), userId);
        FoodRecordImageEntity update = new FoodRecordImageEntity();
        update.setId(exist.getId());
        update.setImageType(normalizeImageType(req.getImageType(), true));
        update.setCaption(normalizeBlank(req.getCaption()));
        update.setSortOrder(req.getSortOrder() == null ? exist.getSortOrder() : req.getSortOrder());
        update.fillUpdateCommonField(userId);
        foodRecordImageMapper.updateById(update);
        return getOwnedImage(req.getId(), userId);
    }

    /**
     * 批量更新美食记录图片排序。
     *
     * @param req 图片排序请求
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortImages(FoodRecordImageSortReq req, Long userId) {
        if (req == null || req.getItems() == null) {
            return;
        }
        for (FoodRecordImageSortReq.Item item : req.getItems()) {
            if (item == null || item.getId() == null || item.getSortOrder() == null) {
                continue;
            }
            FoodRecordImageEntity exist = getOwnedImage(item.getId(), userId);
            FoodRecordImageEntity update = new FoodRecordImageEntity();
            update.setId(exist.getId());
            update.setSortOrder(item.getSortOrder());
            update.fillUpdateCommonField(userId);
            foodRecordImageMapper.updateById(update);
        }
    }

    /**
     * 逻辑删除美食记录图片。
     *
     * @param id 图片 ID
     * @param userId 当前用户 ID
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(Long id, Long userId) {
        FoodRecordImageEntity exist = getOwnedImage(id, userId);
        exist.setIsDeleted(StatusConst.IS_DELETE);
        exist.fillUpdateCommonField(userId);
        foodRecordImageMapper.updateById(exist);
    }

    /**
     * 查询当前用户拥有的图片。
     *
     * @param id 图片 ID
     * @param userId 当前用户 ID
     * @return 图片记录
     *
     * @author Ethan
     * @date 2026-06-01
     */
    @Override
    public FoodRecordImageEntity getOwnedImage(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("图片 ID 不能为空");
        }
        LambdaQueryWrapper<FoodRecordImageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodRecordImageEntity::getId, id);
        wrapper.eq(FoodRecordImageEntity::getUserId, userId);
        wrapper.eq(FoodRecordImageEntity::getIsDeleted, StatusConst.NO_DELETE);
        FoodRecordImageEntity image = foodRecordImageMapper.selectOne(wrapper);
        if (image == null) {
            throw new IllegalArgumentException("图片不存在或无权操作");
        }
        ensureOwnedRecord(image.getRecordId(), userId);
        return image;
    }

    private void ensureOwnedRecord(Long recordId, Long userId) {
        if (recordId == null) {
            throw new IllegalArgumentException("美食记录 ID 不能为空");
        }
        LambdaQueryWrapper<FoodRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodRecordEntity::getId, recordId);
        wrapper.eq(FoodRecordEntity::getUserId, userId);
        FoodRecordEntity record = foodRecordMapper.selectOne(wrapper);
        if (record == null || !Objects.equals(record.getIsDeleted(), StatusConst.NO_DELETE)) {
            throw new IllegalArgumentException("美食记录不存在或无权操作");
        }
    }

    private Integer nextSortOrder(Long recordId, Long userId, String imageType) {
        QueryWrapper<FoodRecordImageEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId);
        wrapper.eq("user_id", userId);
        wrapper.eq("image_type", imageType);
        return Math.max(foodRecordImageMapper.selectCount(wrapper).intValue(), 0);
    }

    private String normalizeImageType(String imageType, boolean useDefault) {
        if (!StringUtils.hasText(imageType)) {
            return useDefault ? DEFAULT_IMAGE_TYPE : null;
        }
        String normalized = imageType.trim().toLowerCase();
        if (!ALLOWED_IMAGE_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("图片类型不支持: " + imageType);
        }
        return normalized;
    }

    private String detectExt(String filename, String contentType) {
        if (StringUtils.hasText(filename) && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1).trim().toLowerCase();
            if (StringUtils.hasText(ext)) {
                return ext;
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

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
