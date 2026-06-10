package top.aiolife.mcp.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.pojo.req.ThoughtQueryToolReq;
import top.aiolife.mcp.pojo.req.ThoughtSaveToolEventReq;
import top.aiolife.mcp.pojo.req.ThoughtSaveToolReq;
import top.aiolife.mcp.pojo.vo.ThoughtQueryToolResp;
import top.aiolife.record.mapper.IRelaEventMapper;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.pojo.entity.ThoughtEntity;
import top.aiolife.record.pojo.entity.ThoughtRelaEventEntity;
import top.aiolife.record.pojo.req.ThoughtSaveEventReq;
import top.aiolife.record.pojo.req.ThoughtSaveReq;
import top.aiolife.record.service.IThoughtService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 闪念 MCP 工具，提供闪念查询和保存能力。
 *
 * @author Ethan
 * @date 2026-06-10
 */
@Component
@RequiredArgsConstructor
public class ThoughtMcpTools {

    private static final int DEFAULT_PAGE = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 50;

    private static final int SUMMARY_LIMIT = 120;

    private static final int CONTENT_LIMIT = 1000;

    private static final int EVENT_LIMIT = 5;

    private static final int EVENT_CONTENT_LIMIT = 300;

    private static final Set<String> ALLOWED_THEME_KEYS = Set.of(
            "blue", "cyan", "green", "purple", "pink", "orange", "teal", "indigo"
    );

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "pending", "ongoing", "done", "shelved", "archived"
    );

    private static final Map<String, String> CATEGORY_NAMES = Map.of(
            "cyan", "工作",
            "green", "生活",
            "teal", "健康",
            "blue", "学习",
            "pink", "社交",
            "purple", "创作",
            "indigo", "AIO-LIFE开发",
            "orange", "旅行"
    );

    private static final Map<String, String> STATUS_NAMES = Map.of(
            "pending", "待处理",
            "ongoing", "进行中",
            "done", "已完成",
            "shelved", "已搁置",
            "archived", "已归档"
    );

    private final IThoughtService thoughtService;
    private final IThoughtMapper thoughtMapper;
    private final IRelaEventMapper relaEventMapper;

    /**
     * 查询当前用户闪念摘要列表。
     *
     * @param req 闪念查询工具请求，包含关键词、分类、状态、日期范围、事件、分页和排序条件
     * @return 统一返回结构，data 为适合 Agent 阅读的闪念摘要分页结果
     *
     * @author Ethan
     * @date 2026-06-10
     */
    @McpOperation(
            name = "thought_query",
            description = "查询当前用户的闪念记录，支持按关键词、主题、正文、分类、状态和创建日期范围筛选，返回适合 Agent 阅读的摘要列表"
    )
    public ApiResponse<ThoughtQueryToolResp> thoughtQuery(ThoughtQueryToolReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        ThoughtQueryToolReq safeReq = req == null ? new ThoughtQueryToolReq() : req;
        int page = normalizePage(safeReq.getPage());
        int pageSize = normalizePageSize(safeReq.getPageSize());

        List<Long> keywordEventThoughtIds = queryEventThoughtIds(safeReq.getKeyword());
        List<Long> hasEventThoughtIds = queryEventThoughtIdsForEventFilter(safeReq.getHasEvents());

        LambdaQueryWrapper<ThoughtEntity> wrapper = buildQueryWrapper(
                userId,
                safeReq,
                keywordEventThoughtIds,
                hasEventThoughtIds
        );
        applyOrder(wrapper, safeReq.getSortBy(), safeReq.getSortOrder());

        Page<ThoughtEntity> pageReq = new Page<>(page, pageSize);
        Page<ThoughtEntity> result = thoughtMapper.selectPage(pageReq, wrapper);
        List<ThoughtEntity> records = result.getRecords();
        Map<Long, List<ThoughtRelaEventEntity>> eventMap = queryEvents(records);

        ThoughtQueryToolResp resp = new ThoughtQueryToolResp();
        resp.setPage(page);
        resp.setPageSize(pageSize);
        resp.setTotal(result.getTotal());
        resp.setHasMore((long) page * pageSize < result.getTotal());
        resp.setItems(records.stream().map(record -> toQueryItem(record, eventMap.get(record.getId()))).toList());
        return ApiResponse.success(resp);
    }

    /**
     * 保存一条闪念。
     *
     * @param req 闪念保存工具请求，包含主题、内容、状态、主题色、事件流和幂等键
     * @return 统一返回结构，data 为是否保存成功
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @McpOperation(
            name = "thought_save",
            description = "保存一条想法，并可附带多个关联事件"
    )
    public ApiResponse<Boolean> thoughtSave(ThoughtSaveToolReq req) {
        ThoughtSaveReq saveReq = new ThoughtSaveReq();
        saveReq.setSubject(req.getSubject());
        saveReq.setContent(req.getContent());
        saveReq.setThemeKey(req.getThemeKey());
        saveReq.setStatus(req.getStatus());

        List<ThoughtSaveToolEventReq> events = req.getEvents();
        if (events != null) {
            saveReq.setEvents(events.stream().map(e -> {
                ThoughtSaveEventReq eventReq = new ThoughtSaveEventReq();
                eventReq.setContent(e.getContent());
                return eventReq;
            }).toList());
        }

        long userId = StpUtil.getLoginIdAsLong();
        return thoughtService.save(saveReq, userId, req.getIdempotencyKey());
    }

    private LambdaQueryWrapper<ThoughtEntity> buildQueryWrapper(long userId,
                                                               ThoughtQueryToolReq req,
                                                               List<Long> keywordEventThoughtIds,
                                                               List<Long> hasEventThoughtIds) {
        LambdaQueryWrapper<ThoughtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtEntity::getUserId, userId);
        wrapper.eq(ThoughtEntity::getIsDeleted, 0);

        String themeKey = normalizeThemeKey(req.getThemeKey());
        if (StringUtils.hasText(themeKey)) {
            wrapper.eq(ThoughtEntity::getThemeKey, themeKey);
        }

        String status = normalizeStatus(req.getStatus());
        if (StringUtils.hasText(status)) {
            wrapper.eq(ThoughtEntity::getStatus, status);
        }

        if (StringUtils.hasText(req.getSubject())) {
            wrapper.like(ThoughtEntity::getSubject, req.getSubject().trim());
        }

        if (StringUtils.hasText(req.getContent())) {
            wrapper.like(ThoughtEntity::getContent, req.getContent().trim());
        }

        applyDateRange(wrapper, req.getStartDate(), req.getEndDate());
        applyKeywordFilter(wrapper, req.getKeyword(), keywordEventThoughtIds);
        applyEventFilter(wrapper, req.getHasEvents(), hasEventThoughtIds);
        return wrapper;
    }

    private void applyDateRange(LambdaQueryWrapper<ThoughtEntity> wrapper, LocalDate startDate, LocalDate endDate) {
        if (startDate != null) {
            wrapper.ge(ThoughtEntity::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.lt(ThoughtEntity::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }
    }

    private void applyKeywordFilter(LambdaQueryWrapper<ThoughtEntity> wrapper,
                                    String keyword,
                                    List<Long> eventThoughtIds) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String trimmed = keyword.trim();
        wrapper.and(condition -> {
            condition.like(ThoughtEntity::getSubject, trimmed)
                    .or()
                    .like(ThoughtEntity::getContent, trimmed);
            if (!eventThoughtIds.isEmpty()) {
                condition.or().in(ThoughtEntity::getId, eventThoughtIds);
            }
        });
    }

    private void applyEventFilter(LambdaQueryWrapper<ThoughtEntity> wrapper,
                                  Boolean hasEvents,
                                  List<Long> eventThoughtIds) {
        if (hasEvents == null) {
            return;
        }
        if (eventThoughtIds.isEmpty()) {
            if (Boolean.TRUE.equals(hasEvents)) {
                wrapper.eq(ThoughtEntity::getId, -1L);
            }
            return;
        }
        if (Boolean.TRUE.equals(hasEvents)) {
            wrapper.in(ThoughtEntity::getId, eventThoughtIds);
        } else {
            wrapper.notIn(ThoughtEntity::getId, eventThoughtIds);
        }
    }

    private void applyOrder(LambdaQueryWrapper<ThoughtEntity> wrapper, String sortBy, String sortOrder) {
        boolean ascending = Objects.equals(normalizeSortOrder(sortOrder), "asc");
        SFunction<ThoughtEntity, ?> sortColumn = Objects.equals(normalizeSortBy(sortBy), "createTime")
                ? ThoughtEntity::getCreateTime
                : ThoughtEntity::getUpdateTime;
        wrapper.orderBy(true, ascending, sortColumn);
        wrapper.orderByDesc(ThoughtEntity::getId);
    }

    private List<Long> queryEventThoughtIds(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<ThoughtRelaEventEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ThoughtRelaEventEntity::getThoughtId);
        wrapper.eq(ThoughtRelaEventEntity::getIsDeleted, 0);
        wrapper.like(ThoughtRelaEventEntity::getContent, keyword.trim());
        return distinctThoughtIds(relaEventMapper.selectList(wrapper));
    }

    private List<Long> queryEventThoughtIdsForEventFilter(Boolean hasEvents) {
        if (hasEvents == null) {
            return List.of();
        }
        LambdaQueryWrapper<ThoughtRelaEventEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ThoughtRelaEventEntity::getThoughtId);
        wrapper.eq(ThoughtRelaEventEntity::getIsDeleted, 0);
        return distinctThoughtIds(relaEventMapper.selectList(wrapper));
    }

    private List<Long> distinctThoughtIds(List<ThoughtRelaEventEntity> events) {
        return events.stream()
                .map(ThoughtRelaEventEntity::getThoughtId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private Map<Long, List<ThoughtRelaEventEntity>> queryEvents(List<ThoughtEntity> records) {
        List<Long> thoughtIds = records.stream().map(ThoughtEntity::getId).filter(Objects::nonNull).toList();
        if (thoughtIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<ThoughtRelaEventEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ThoughtRelaEventEntity::getThoughtId, thoughtIds);
        wrapper.eq(ThoughtRelaEventEntity::getIsDeleted, 0);
        wrapper.orderByAsc(ThoughtRelaEventEntity::getCreateTime);
        return relaEventMapper.selectList(wrapper).stream()
                .collect(Collectors.groupingBy(
                        ThoughtRelaEventEntity::getThoughtId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private ThoughtQueryToolResp.Item toQueryItem(ThoughtEntity record, List<ThoughtRelaEventEntity> events) {
        ThoughtQueryToolResp.Item item = new ThoughtQueryToolResp.Item();
        item.setId(record.getId());
        item.setSubject(record.getSubject());
        item.setSummary(truncate(record.getContent(), SUMMARY_LIMIT));
        item.setContent(truncate(record.getContent(), CONTENT_LIMIT));
        item.setThemeKey(record.getThemeKey());
        item.setCategoryName(categoryName(record.getThemeKey()));
        item.setStatus(record.getStatus());
        item.setStatusName(statusName(record.getStatus()));
        item.setCreateTime(record.getCreateTime());
        item.setUpdateTime(record.getUpdateTime());
        item.setEvents(toQueryEvents(events));
        return item;
    }

    private List<ThoughtQueryToolResp.Event> toQueryEvents(List<ThoughtRelaEventEntity> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        List<ThoughtQueryToolResp.Event> result = new ArrayList<>();
        for (ThoughtRelaEventEntity event : events) {
            if (result.size() >= EVENT_LIMIT) {
                break;
            }
            ThoughtQueryToolResp.Event item = new ThoughtQueryToolResp.Event();
            item.setId(event.getId());
            item.setContent(truncate(event.getContent(), EVENT_CONTENT_LIMIT));
            item.setCreateTime(event.getCreateTime());
            result.add(item);
        }
        return result;
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String normalizeThemeKey(String themeKey) {
        if (!StringUtils.hasText(themeKey)) {
            return "";
        }
        String normalized = themeKey.trim().toLowerCase();
        return ALLOWED_THEME_KEYS.contains(normalized) ? normalized : "";
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "";
        }
        String trimmed = status.trim();
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
            default -> "";
        };
    }

    private String normalizeSortBy(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "updateTime";
        }
        String normalized = sortBy.trim();
        return Objects.equals(normalized, "createTime") ? "createTime" : "updateTime";
    }

    private String normalizeSortOrder(String sortOrder) {
        if (!StringUtils.hasText(sortOrder)) {
            return "desc";
        }
        return Objects.equals(sortOrder.trim().toLowerCase(), "asc") ? "asc" : "desc";
    }

    private String categoryName(String themeKey) {
        String normalized = normalizeThemeKey(themeKey);
        return StringUtils.hasText(normalized) ? CATEGORY_NAMES.getOrDefault(normalized, "未分类") : "未分类";
    }

    private String statusName(String status) {
        String normalized = normalizeStatus(status);
        return StringUtils.hasText(normalized) ? STATUS_NAMES.getOrDefault(normalized, "未知状态") : "未知状态";
    }

    private String truncate(String value, int limit) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= limit) {
            return trimmed;
        }
        return trimmed.substring(0, limit);
    }
}
