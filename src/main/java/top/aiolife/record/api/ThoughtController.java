package top.aiolife.record.api;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import top.aiolife.core.query.CommonQuery;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.core.resq.PageResp;
import top.aiolife.config.MinioConfig;
import top.aiolife.core.util.MinioUtil;
import top.aiolife.record.mapper.IRelaEventMapper;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.pojo.entity.ThoughtRelaEventEntity;
import top.aiolife.record.pojo.entity.ThoughtEntity;
import top.aiolife.record.pojo.req.CommonReq;
import top.aiolife.record.pojo.req.ThoughtSaveReq;
import top.aiolife.record.service.IThoughtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 闪念（思考）接口
 *
 * @author Ethan
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/thought")
public class ThoughtController {
    private final IThoughtMapper thoughtMapper;

    private final IRelaEventMapper relaEventMapper;

    private final IThoughtService thoughtService;

    private final MinioUtil minioUtil;

    private final MinioConfig minioConfig;

    @Value("${aio.life.serve.base-url}")
    private String serveBaseUrl;

    private static final Set<String> ALLOWED_THEME_KEYS = Set.of(
            "blue", "cyan", "green", "purple", "pink", "orange", "teal", "indigo"
    );

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "pending", "ongoing", "done", "archived"
    );

    private static String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        String trimmed = status.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        String lower = trimmed.toLowerCase();
        if (ALLOWED_STATUSES.contains(lower)) {
            return lower;
        }
        return switch (trimmed) {
            case "待处理" -> "pending";
            case "进行中" -> "ongoing";
            case "已完成" -> "done";
            case "已归档" -> "archived";
            default -> null;
        };
    }

    public IThoughtMapper getBaseMapper() {
        return thoughtMapper;
    }

    @PostMapping("/query")
    public ApiResponse<PageResp<ThoughtEntity>> query(
            @RequestBody CommonQuery<ThoughtEntity> query) {
        long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<ThoughtEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ThoughtEntity::getUserId, userId);
        ThoughtEntity condition = query.getCondition();
        if (condition != null) {
            String themeKey = condition.getThemeKey();
            if (themeKey != null) {
                String trimmed = themeKey.trim();
                if (!trimmed.isBlank() && ALLOWED_THEME_KEYS.contains(trimmed)) {
                    lambdaQueryWrapper.eq(ThoughtEntity::getThemeKey, trimmed);
                }
            }

            String normalizedStatus = normalizeStatus(condition.getStatus());
            if (normalizedStatus != null) {
                lambdaQueryWrapper.eq(ThoughtEntity::getStatus, normalizedStatus);
            }
        }

        lambdaQueryWrapper.orderByDesc(ThoughtEntity::getUpdateTime);
        Page<ThoughtEntity> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<ThoughtEntity> iPage = thoughtMapper.selectPage(page, lambdaQueryWrapper);


        // 查询明细
        List<Long> thoughtIdList = iPage.getRecords().stream().map(ThoughtEntity::getId).toList();
        if (!thoughtIdList.isEmpty()) {
            LambdaQueryWrapper<ThoughtRelaEventEntity> relaEventLambdaQueryWrapper = new LambdaQueryWrapper<>();
            relaEventLambdaQueryWrapper.in(ThoughtRelaEventEntity::getThoughtId, thoughtIdList);
            List<ThoughtRelaEventEntity> thoughtRelaEventEntityList = relaEventMapper.selectList(relaEventLambdaQueryWrapper);
            // 关联事件
            iPage.getRecords().forEach(thoughtVO -> {
                List<ThoughtRelaEventEntity> eventEntityList = thoughtRelaEventEntityList.stream().filter(eventEntity -> eventEntity.getThoughtId().equals(thoughtVO.getId())).toList();
                thoughtVO.setEvents(eventEntityList);
            });
        }

        PageResp<ThoughtEntity> objectPageResp = PageResp.of(iPage.getRecords(), iPage.getTotal());

        return ApiResponse.success(objectPageResp);
    }
    
    @PostMapping("/save")
    public ApiResponse<Boolean> save(@RequestBody ThoughtSaveReq req) {
        long loginId = StpUtil.getLoginIdAsLong();
        return thoughtService.save(req, loginId, null);
    }

    @PostMapping("/update")
    public ApiResponse<Boolean> update(@RequestBody ThoughtEntity entity) {
        Long userId = StpUtil.getLoginIdAsLong();
        entity.setUserId(userId);
        entity.setUpdateTime(LocalDateTime.now());

        String subject = entity.getSubject() == null ? null : entity.getSubject().trim();
        if (subject == null || subject.isBlank()) {
            String content = entity.getContent() == null ? "" : entity.getContent().trim();
            String firstLine = content.split("\\R", 2)[0].trim();
            subject = firstLine.isBlank() ? null : (firstLine.length() > 60 ? firstLine.substring(0, 60) : firstLine);
        }
        if (subject != null) {
            entity.setSubject(subject);
        }

        String themeKey = entity.getThemeKey();
        if (themeKey != null && !ALLOWED_THEME_KEYS.contains(themeKey)) {
            entity.setThemeKey(null);
        }

        String normalizedStatus = normalizeStatus(entity.getStatus());
        if (entity.getStatus() != null && normalizedStatus == null) {
            entity.setStatus(null);
        } else if (normalizedStatus != null) {
            entity.setStatus(normalizedStatus);
        }
        
        LambdaQueryWrapper<ThoughtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtEntity::getId, entity.getId());
        wrapper.eq(ThoughtEntity::getUserId, userId);
        
        int rows = getBaseMapper().update(entity, wrapper);
        
        if (rows > 0) {
            // 更新事件
            List<ThoughtRelaEventEntity> events = entity.getEvents();
            if (events != null) {
                events.forEach(eventEntity -> {
                    eventEntity.setThoughtId(entity.getId());
                    relaEventMapper.insertOrUpdate(eventEntity);
                });
            }
            return ApiResponse.success(true);
        }
        return ApiResponse.error("无权操作或记录不存在");
    }

    /**
     * 上传闪念卡图片。
     *
     * <p>用途：前端为某条闪念上传卡片图片，后端将图片写入 MinIO，并把对象键保存到 thought.card_object。</p>
     *
     * @param id 闪念 ID
     * @param file 图片文件（multipart/form-data，字段名为 file）
     * @return 统一返回结构，data 包含 cardObject 与 cardUrl
     */
    @PostMapping("/{id}/card/upload")
    public ApiResponse<Map<String, Object>> uploadCard(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        if (id == null) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, "id 不能为空");
        }
        if (file == null || file.isEmpty()) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, "文件不能为空");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, "仅支持图片文件");
        }
        long userId = StpUtil.getLoginIdAsLong();
        ThoughtEntity exist = thoughtMapper.selectById(id);
        if (exist == null || !Objects.equals(exist.getIsDeleted(), 0) || !Objects.equals(exist.getUserId(), userId)) {
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "无权操作或记录不存在");
        }

        String ext = detectExt(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String objectName = "think-card/" + id + "/" + filename;
        String bucketName = StringUtils.hasText(minioConfig.getBucketName()) ? minioConfig.getBucketName() : "aiolife";

        try {
            minioUtil.uploadFile(bucketName, file, objectName);

            ThoughtEntity update = new ThoughtEntity();
            update.setCardObject(objectName);
            update.fillUpdateCommonField(userId);

            LambdaUpdateWrapper<ThoughtEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ThoughtEntity::getId, id);
            wrapper.eq(ThoughtEntity::getUserId, userId);
            wrapper.eq(ThoughtEntity::getIsDeleted, 0);
            thoughtMapper.update(update, wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("cardObject", objectName);
            data.put("cardUrl", serveBaseUrl + "/file/preview/" + bucketName + "/" + objectName);
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除
     */
    @PostMapping("/batchDelete")
    public ApiResponse<Boolean> delete(@RequestBody CommonReq CommonReq) {
        LambdaUpdateWrapper<ThoughtEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(ThoughtEntity::getUserId, StpUtil.getLoginIdAsLong());
        lambdaUpdateWrapper.in(ThoughtEntity::getId, CommonReq.getIdList());
        getBaseMapper().delete(lambdaUpdateWrapper);
        return ApiResponse.success(true);
    }

    private String detectExt(String filename, String contentType) {
        if (StringUtils.hasText(filename) && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1).trim().toLowerCase();
            if (StringUtils.hasText(ext)) {
                return ext;
            }
        }
        if (Objects.equals("image/jpeg", contentType)) {
            return "jpg";
        }
        if (Objects.equals("image/png", contentType)) {
            return "png";
        }
        if (Objects.equals("image/gif", contentType)) {
            return "gif";
        }
        if (Objects.equals("image/webp", contentType)) {
            return "webp";
        }
        return "png";
    }

}
