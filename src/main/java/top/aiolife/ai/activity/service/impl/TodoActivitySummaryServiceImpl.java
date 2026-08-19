package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.TodoSummary;
import top.aiolife.ai.activity.service.TodoActivitySummaryService;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.ITaskMapper;
import top.aiolife.record.pojo.entity.TaskEntity;

import java.util.List;

/**
 * 待办活动统计服务实现，通过待办数据构建新增数量和内容明细。
 *
 * @author Ethan
 * @date 2026-08-16
 */
@Service
@RequiredArgsConstructor
public class TodoActivitySummaryServiceImpl implements TodoActivitySummaryService {

    private final ITaskMapper taskMapper;

    /**
     * 汇总指定用户在活动周期内新增的待办。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 待办活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-16
     */
    @Override
    public TodoSummary summarize(Long userId, AiActivityDateRange range) {
        ActivitySummarySupport.validate(userId, range);
        LambdaQueryWrapper<TaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(TaskEntity::getContent, TaskEntity::getCreateTime);
        wrapper.eq(TaskEntity::getUserId, userId);
        wrapper.eq(TaskEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.ge(TaskEntity::getCreateTime, range.getStartTime());
        wrapper.lt(TaskEntity::getCreateTime, range.getEndTime());
        wrapper.orderByDesc(TaskEntity::getCreateTime);
        List<TaskEntity> records = ActivitySummarySupport.filterValidRecords(
                "todo", taskMapper.selectList(wrapper), TaskEntity::getContent);

        TodoSummary summary = new TodoSummary();
        summary.setNewCount(records.size());
        summary.setContents(ActivitySummarySupport.detailTexts(records, TaskEntity::getContent));
        return summary;
    }
}
