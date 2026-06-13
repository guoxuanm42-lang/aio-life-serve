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
import top.aiolife.record.pojo.vo.ThoughtDetailVO;
import top.aiolife.record.service.IThoughtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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
 * @date 2026-06-05
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
            "pending", "ongoing", "done", "shelved", "archived"
    );

    private static final Set<String> ALLOWED_THOUGHT_TYPES = Set.of(
            "action", "emotion", "reflection"
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
            case "已搁置" -> "shelved";
            case "已归档" -> "archived";
            default -> null;
        };
    }

    private static String normalizeThoughtTypeFilter(String thoughtType) {
        if (!StringUtils.hasText(thoughtType)) {
            return null;
        }
        String normalized = thoughtType.trim().toLowerCase();
        return ALLOWED_THOUGHT_TYPES.contains(normalized) ? normalized : null;
    }

    private static String normalizeThoughtTypeOrDefault(String thoughtType) {
        String normalized = normalizeThoughtTypeFilter(thoughtType);
        return normalized == null ? "action" : normalized;
    }

    public IThoughtMapper getBaseMapper() {
        return thoughtMapper;
    }

    /**
     * 查询当前用户闪念列表。
     *
     * <p>用途：按分类、状态和主题内容筛选闪念，返回分页列表与关联事件。</p>
     *
     * @param query 查询请求，condition 可包含 themeKey、status、subject
     * @return 统一返回结构，data 为分页闪念列表
     *
     * @author Ethan
     * @date 2026-06-05
     */
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

            String normalizedThoughtType = normalizeThoughtTypeFilter(condition.getThoughtType());
            if (normalizedThoughtType != null) {
                lambdaQueryWrapper.eq(ThoughtEntity::getThoughtType, normalizedThoughtType);
            }

            String subject = condition.getSubject();
            if (StringUtils.hasText(subject)) {
                lambdaQueryWrapper.like(ThoughtEntity::getSubject, subject.trim());
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
            relaEventLambdaQueryWrapper.eq(ThoughtRelaEventEntity::getIsDeleted, 0);
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

    /**
     * 更新当前用户的闪念记录。
     *
     * <p>用途：前端提交闪念主表、事件流和结构化详情信息，后端校验归属后完成更新。</p>
     *
     * @param req 闪念更新请求体，包含 id、subject、content、status、thoughtType、events 和 detail 信息
     * @return 统一返回结构，data 表示是否更新成功
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @PostMapping("/update")
    public ApiResponse<Boolean> update(@RequestBody ThoughtSaveReq req) {
        long loginId = StpUtil.getLoginIdAsLong();
        return thoughtService.update(req, loginId);
    }

    /**
     * 查询当前用户的闪念详情。
     *
     * <p>用途：前端打开编辑或详情弹窗时，按闪念 ID 获取主记录、事件流、三类结构化详情和状态日志。</p>
     *
     * @param id 闪念 ID
     * @return 统一返回结构，data 为闪念详情；无权限或记录不存在时返回空
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<ThoughtDetailVO> detail(@PathVariable Long id) {
        long loginId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(thoughtService.detail(id, loginId));
    }

    /**
     * ?????????
     *
     * <p>????????? ID ???????????????????????</p>
     *
     * @param commonReq ?????????? idList
     * @return ???????data ????????
     *
     * @author Ethan
     * @date 2026-06-12
     */
    @PostMapping("/batchDelete")
    public ApiResponse<Boolean> delete(@RequestBody CommonReq commonReq) {
        return thoughtService.batchDelete(commonReq, StpUtil.getLoginIdAsLong());
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
