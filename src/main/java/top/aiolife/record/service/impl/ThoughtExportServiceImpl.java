package top.aiolife.record.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.record.mapper.IRelaEventMapper;
import top.aiolife.record.mapper.IThoughtActionDetailMapper;
import top.aiolife.record.mapper.IThoughtEmotionDetailMapper;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.mapper.IThoughtReflectionDetailMapper;
import top.aiolife.record.mapper.IThoughtStatusLogMapper;
import top.aiolife.record.pojo.entity.ThoughtActionDetailEntity;
import top.aiolife.record.pojo.entity.ThoughtEmotionDetailEntity;
import top.aiolife.record.pojo.entity.ThoughtEntity;
import top.aiolife.record.pojo.entity.ThoughtReflectionDetailEntity;
import top.aiolife.record.pojo.entity.ThoughtRelaEventEntity;
import top.aiolife.record.pojo.entity.ThoughtStatusLogEntity;
import top.aiolife.record.pojo.req.ThoughtExportReq;
import top.aiolife.record.service.IThoughtExportService;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 闪念数据 Excel 导出服务实现。
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Service
@RequiredArgsConstructor
public class ThoughtExportServiceImpl implements IThoughtExportService {

    private static final int EXPORT_LIMIT = 10_000;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Set<String> ALLOWED_THEME_KEYS = Set.of(
            "blue", "cyan", "green", "purple", "pink", "orange", "teal", "indigo"
    );

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "pending", "ongoing", "done", "shelved", "archived"
    );

    private static final Set<String> ALLOWED_THOUGHT_TYPES = Set.of(
            "action", "emotion", "reflection"
    );

    private final IThoughtMapper thoughtMapper;

    private final IRelaEventMapper relaEventMapper;

    private final IThoughtActionDetailMapper actionDetailMapper;

    private final IThoughtEmotionDetailMapper emotionDetailMapper;

    private final IThoughtReflectionDetailMapper reflectionDetailMapper;

    private final IThoughtStatusLogMapper statusLogMapper;

    /**
     * 校验当前筛选范围是否允许同步导出。
     *
     * @param userId 当前登录用户 ID
     * @param req 导出筛选请求，支持分类、状态、类型和主题筛选
     *
     * @author Ethan
     * @date 2026-06-13
     */
    @Override
    public void validateExportable(long userId, ThoughtExportReq req) {
        Long total = thoughtMapper.selectCount(buildThoughtQuery(userId, req));
        if (total != null && total > EXPORT_LIMIT) {
            throw new IllegalArgumentException("导出数据超过 " + EXPORT_LIMIT + " 条，请缩小筛选范围");
        }
    }

    /**
     * 导出当前用户的闪念数据到 Excel 输出流。
     *
     * @param userId 当前登录用户 ID
     * @param req 导出筛选请求，支持分类、状态、类型和主题筛选
     * @param outputStream Excel 文件输出流
     *
     * @author Ethan
     * @date 2026-06-13
     */
    @Override
    public void exportThoughts(long userId, ThoughtExportReq req, OutputStream outputStream) {
        validateExportable(userId, req);
        LambdaQueryWrapper<ThoughtEntity> queryWrapper = buildThoughtQuery(userId, req);
        queryWrapper.orderByDesc(ThoughtEntity::getCreateTime);
        List<ThoughtEntity> thoughts = thoughtMapper.selectList(queryWrapper);
        List<Long> thoughtIds = thoughts.stream().map(ThoughtEntity::getId).toList();

        ExcelWriter excelWriter = EasyExcel.write(outputStream).build();
        try {
            writeSheet(excelWriter, 0, "闪念主表", thoughtHead(), thoughtRows(thoughts));
            writeSheet(excelWriter, 1, "事件流", eventHead(), eventRows(listEvents(thoughtIds)));
            writeSheet(excelWriter, 2, "行动详情", actionDetailHead(), actionDetailRows(listActionDetails(userId, thoughtIds)));
            writeSheet(excelWriter, 3, "情绪详情", emotionDetailHead(), emotionDetailRows(listEmotionDetails(userId, thoughtIds)));
            writeSheet(excelWriter, 4, "复盘详情", reflectionDetailHead(), reflectionDetailRows(listReflectionDetails(userId, thoughtIds)));
            writeSheet(excelWriter, 5, "状态日志", statusLogHead(), statusLogRows(listStatusLogs(userId, thoughtIds)));
        } finally {
            excelWriter.finish();
        }
    }

    private LambdaQueryWrapper<ThoughtEntity> buildThoughtQuery(long userId, ThoughtExportReq req) {
        LambdaQueryWrapper<ThoughtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtEntity::getUserId, userId);
        wrapper.eq(ThoughtEntity::getIsDeleted, 0);
        if (req == null) {
            return wrapper;
        }

        String themeKey = normalize(req.getThemeKey());
        if (themeKey != null && ALLOWED_THEME_KEYS.contains(themeKey)) {
            wrapper.eq(ThoughtEntity::getThemeKey, themeKey);
        }

        String status = normalize(req.getStatus());
        if (status != null && ALLOWED_STATUSES.contains(status)) {
            wrapper.eq(ThoughtEntity::getStatus, status);
        }

        String thoughtType = normalize(req.getThoughtType());
        if (thoughtType != null && ALLOWED_THOUGHT_TYPES.contains(thoughtType)) {
            wrapper.eq(ThoughtEntity::getThoughtType, thoughtType);
        }

        if (StringUtils.hasText(req.getSubject())) {
            wrapper.like(ThoughtEntity::getSubject, req.getSubject().trim());
        }
        return wrapper;
    }

    private List<ThoughtRelaEventEntity> listEvents(List<Long> thoughtIds) {
        if (thoughtIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ThoughtRelaEventEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ThoughtRelaEventEntity::getThoughtId, thoughtIds);
        wrapper.eq(ThoughtRelaEventEntity::getIsDeleted, 0);
        wrapper.orderByAsc(ThoughtRelaEventEntity::getCreateTime);
        return relaEventMapper.selectList(wrapper);
    }

    private List<ThoughtActionDetailEntity> listActionDetails(long userId, List<Long> thoughtIds) {
        if (thoughtIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ThoughtActionDetailEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtActionDetailEntity::getUserId, userId);
        wrapper.in(ThoughtActionDetailEntity::getThoughtId, thoughtIds);
        wrapper.eq(ThoughtActionDetailEntity::getIsDeleted, 0);
        return actionDetailMapper.selectList(wrapper);
    }

    private List<ThoughtEmotionDetailEntity> listEmotionDetails(long userId, List<Long> thoughtIds) {
        if (thoughtIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ThoughtEmotionDetailEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtEmotionDetailEntity::getUserId, userId);
        wrapper.in(ThoughtEmotionDetailEntity::getThoughtId, thoughtIds);
        wrapper.eq(ThoughtEmotionDetailEntity::getIsDeleted, 0);
        return emotionDetailMapper.selectList(wrapper);
    }

    private List<ThoughtReflectionDetailEntity> listReflectionDetails(long userId, List<Long> thoughtIds) {
        if (thoughtIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ThoughtReflectionDetailEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtReflectionDetailEntity::getUserId, userId);
        wrapper.in(ThoughtReflectionDetailEntity::getThoughtId, thoughtIds);
        wrapper.eq(ThoughtReflectionDetailEntity::getIsDeleted, 0);
        return reflectionDetailMapper.selectList(wrapper);
    }

    private List<ThoughtStatusLogEntity> listStatusLogs(long userId, List<Long> thoughtIds) {
        if (thoughtIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ThoughtStatusLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThoughtStatusLogEntity::getUserId, userId);
        wrapper.in(ThoughtStatusLogEntity::getThoughtId, thoughtIds);
        wrapper.eq(ThoughtStatusLogEntity::getIsDeleted, 0);
        wrapper.orderByAsc(ThoughtStatusLogEntity::getCreateTime);
        return statusLogMapper.selectList(wrapper);
    }

    private void writeSheet(ExcelWriter excelWriter, int index, String sheetName, List<List<String>> head, List<List<Object>> rows) {
        WriteSheet sheet = EasyExcel.writerSheet(index, sheetName).head(head).build();
        excelWriter.write(rows, sheet);
    }

    private List<List<String>> thoughtHead() {
        return head("id", "类型", "主题", "内容", "分类", "状态", "创建时间", "更新时间");
    }

    private List<List<Object>> thoughtRows(List<ThoughtEntity> thoughts) {
        return thoughts.stream()
                .map(item -> row(
                        item.getId(),
                        thoughtTypeLabel(item.getThoughtType()),
                        item.getSubject(),
                        item.getContent(),
                        themeLabel(item.getThemeKey()),
                        statusLabel(item.getStatus()),
                        formatDateTime(item.getCreateTime()),
                        formatDateTime(item.getUpdateTime())
                ))
                .toList();
    }

    private List<List<String>> eventHead() {
        return head("thoughtId", "事件内容", "事件时间");
    }

    private List<List<Object>> eventRows(List<ThoughtRelaEventEntity> events) {
        return events.stream()
                .map(item -> row(item.getThoughtId(), item.getContent(), formatDateTime(item.getCreateTime())))
                .toList();
    }

    private List<List<String>> actionDetailHead() {
        return head("thoughtId", "结果总结", "复盘", "下一步行动", "搁置原因", "搁置标签", "重启策略", "归档原因", "价值等级", "归档类型");
    }

    private List<List<Object>> actionDetailRows(List<ThoughtActionDetailEntity> details) {
        return details.stream()
                .map(item -> row(
                        item.getThoughtId(),
                        item.getResultSummary(),
                        item.getReflection(),
                        item.getNextAction(),
                        item.getShelveReason(),
                        item.getShelveReasonTag(),
                        item.getRestartPolicy(),
                        item.getArchiveReason(),
                        item.getValueLevel(),
                        item.getArchiveType()
                ))
                .toList();
    }

    private List<List<String>> emotionDetailHead() {
        return head("thoughtId", "情绪类型", "情绪强度", "触发因素", "情绪需求", "应对行动", "复盘总结", "忽略原因");
    }

    private List<List<Object>> emotionDetailRows(List<ThoughtEmotionDetailEntity> details) {
        return details.stream()
                .map(item -> row(
                        item.getThoughtId(),
                        item.getEmotionType(),
                        item.getEmotionIntensity(),
                        item.getEmotionTrigger(),
                        item.getEmotionNeed(),
                        item.getCopingAction(),
                        item.getReflectionSummary(),
                        item.getIgnoredReason()
                ))
                .toList();
    }

    private List<List<String>> reflectionDetailHead() {
        return head("thoughtId", "复盘总结", "经验类型", "归档类型", "价值等级", "改进行动", "关联项目", "标签");
    }

    private List<List<Object>> reflectionDetailRows(List<ThoughtReflectionDetailEntity> details) {
        return details.stream()
                .map(item -> row(
                        item.getThoughtId(),
                        item.getReflectionSummary(),
                        item.getLessonType(),
                        item.getArchiveType(),
                        item.getValueLevel(),
                        item.getImprovementAction(),
                        item.getRelatedProject(),
                        item.getTags()
                ))
                .toList();
    }

    private List<List<String>> statusLogHead() {
        return head("thoughtId", "类型", "原状态", "新状态", "变更原因", "时间");
    }

    private List<List<Object>> statusLogRows(List<ThoughtStatusLogEntity> logs) {
        return logs.stream()
                .map(item -> row(
                        item.getThoughtId(),
                        thoughtTypeLabel(item.getThoughtType()),
                        statusLabel(item.getFromStatus()),
                        statusLabel(item.getToStatus()),
                        item.getChangeReason(),
                        formatDateTime(item.getCreateTime())
                ))
                .toList();
    }

    private List<List<String>> head(String... names) {
        return List.of(names).stream()
                .map(List::of)
                .toList();
    }

    private List<Object> row(Object... values) {
        return Arrays.asList(values);
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME_FORMATTER.format(dateTime);
    }

    private String thoughtTypeLabel(String thoughtType) {
        if ("emotion".equals(thoughtType)) {
            return "情绪心情";
        }
        if ("reflection".equals(thoughtType)) {
            return "复盘沉淀";
        }
        return "想法行动";
    }

    private String statusLabel(String status) {
        if ("pending".equals(status)) {
            return "待处理";
        }
        if ("ongoing".equals(status)) {
            return "进行中";
        }
        if ("done".equals(status)) {
            return "已完成";
        }
        if ("shelved".equals(status)) {
            return "已搁置";
        }
        if ("archived".equals(status)) {
            return "已归档";
        }
        return status == null ? "" : status;
    }

    private String themeLabel(String themeKey) {
        if ("cyan".equals(themeKey)) {
            return "工作";
        }
        if ("green".equals(themeKey)) {
            return "生活";
        }
        if ("teal".equals(themeKey)) {
            return "健康";
        }
        if ("blue".equals(themeKey)) {
            return "学习";
        }
        if ("pink".equals(themeKey)) {
            return "社交";
        }
        if ("purple".equals(themeKey)) {
            return "创作";
        }
        if ("indigo".equals(themeKey)) {
            return "AIO-LIFE开发";
        }
        if ("orange".equals(themeKey)) {
            return "旅行";
        }
        return "未分类";
    }
}
