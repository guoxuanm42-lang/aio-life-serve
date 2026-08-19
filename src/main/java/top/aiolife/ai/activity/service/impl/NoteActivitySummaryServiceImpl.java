package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.NoteSummary;
import top.aiolife.ai.activity.service.NoteActivitySummaryService;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.IMemoMapper;
import top.aiolife.record.pojo.entity.MemoEntity;

import java.util.List;

/**
 * 笔记活动统计服务实现，仅读取笔记标题并构建新增统计。
 *
 * @author Ethan
 * @date 2026-08-16
 */
@Service
@RequiredArgsConstructor
public class NoteActivitySummaryServiceImpl implements NoteActivitySummaryService {

    private final IMemoMapper memoMapper;

    /**
     * 汇总指定用户在活动周期内新增的笔记。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 笔记活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-16
     */
    @Override
    public NoteSummary summarize(Long userId, AiActivityDateRange range) {
        ActivitySummarySupport.validate(userId, range);
        LambdaQueryWrapper<MemoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(MemoEntity::getTitle, MemoEntity::getCreateTime);
        wrapper.eq(MemoEntity::getUserId, userId);
        wrapper.eq(MemoEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.ge(MemoEntity::getCreateTime, range.getStartTime());
        wrapper.lt(MemoEntity::getCreateTime, range.getEndTime());
        wrapper.orderByDesc(MemoEntity::getCreateTime);
        List<MemoEntity> records = ActivitySummarySupport.filterValidRecords(
                "note", memoMapper.selectList(wrapper), MemoEntity::getTitle);

        NoteSummary summary = new NoteSummary();
        summary.setNewCount(records.size());
        summary.setTitles(ActivitySummarySupport.detailTexts(records, MemoEntity::getTitle));
        return summary;
    }
}
