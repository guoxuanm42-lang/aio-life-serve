package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.ThoughtSummary;
import top.aiolife.ai.activity.service.ThoughtActivitySummaryService;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.pojo.entity.ThoughtEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 闪念活动统计服务实现，通过闪念数据构建新增、类型、主题和标题统计。
 *
 * @author Ethan
 * @date 2026-08-13
 */
@Service
@RequiredArgsConstructor
public class ThoughtActivitySummaryServiceImpl implements ThoughtActivitySummaryService {

    private static final Map<String, String> TYPE_NAMES = typeNames();
    private static final Map<String, String> THEME_NAMES = themeNames();

    private final IThoughtMapper thoughtMapper;

    /**
     * 汇总指定用户在活动周期内新增的闪念。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 闪念活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    @Override
    public ThoughtSummary summarize(Long userId, AiActivityDateRange range) {
        ActivitySummarySupport.validate(userId, range);
        LambdaQueryWrapper<ThoughtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ThoughtEntity::getSubject, ThoughtEntity::getThoughtType,
                ThoughtEntity::getThemeKey, ThoughtEntity::getCreateTime);
        wrapper.eq(ThoughtEntity::getUserId, userId);
        wrapper.eq(ThoughtEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.ge(ThoughtEntity::getCreateTime, range.getStartTime());
        wrapper.lt(ThoughtEntity::getCreateTime, range.getEndTime());
        wrapper.orderByDesc(ThoughtEntity::getCreateTime);
        List<ThoughtEntity> records = thoughtMapper.selectList(wrapper);

        ThoughtSummary summary = new ThoughtSummary();
        summary.setNewCount(records.size());
        summary.setTitles(ActivitySummarySupport.detailTexts(records, ThoughtEntity::getSubject));
        List<String> typeKeys = records.stream().map(item -> normalize(item.getThoughtType(), TYPE_NAMES)).toList();
        List<String> themeKeys = records.stream().map(item -> normalize(item.getThemeKey(), THEME_NAMES)).toList();
        summary.setTypeDistribution(ActivitySummarySupport.buildDistribution(
                ActivitySummarySupport.countKeys(typeKeys), key -> TYPE_NAMES.getOrDefault(key, ActivitySummarySupport.UNCATEGORIZED_NAME)));
        summary.setThemeDistribution(ActivitySummarySupport.buildDistribution(
                ActivitySummarySupport.countKeys(themeKeys), key -> THEME_NAMES.getOrDefault(key, ActivitySummarySupport.UNCATEGORIZED_NAME)));
        return summary;
    }

    private String normalize(String value, Map<String, String> names) {
        if (!StringUtils.hasText(value)) {
            return ActivitySummarySupport.UNCATEGORIZED_KEY;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return names.containsKey(normalized) ? normalized : ActivitySummarySupport.UNCATEGORIZED_KEY;
    }

    private static Map<String, String> typeNames() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("action", "想法行动");
        names.put("emotion", "情绪心情");
        names.put("reflection", "复盘沉淀");
        return names;
    }

    private static Map<String, String> themeNames() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("cyan", "工作");
        names.put("green", "生活");
        names.put("teal", "健康");
        names.put("blue", "学习");
        names.put("pink", "社交");
        names.put("purple", "创作");
        names.put("indigo", "AIO-LIFE开发");
        names.put("orange", "旅行");
        return names;
    }
}
