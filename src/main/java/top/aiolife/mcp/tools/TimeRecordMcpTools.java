package top.aiolife.mcp.tools;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.aiolife.core.resq.ApiResponse;
import top.aiolife.mcp.annotation.McpOperation;
import top.aiolife.mcp.pojo.req.ExerciseRecordToolReq;
import top.aiolife.mcp.pojo.req.TimeRecordDateRangeToolReq;
import top.aiolife.mcp.pojo.req.TimeRecordSaveToolReq;
import top.aiolife.record.pojo.req.ExerciseRecordReq;
import top.aiolife.record.pojo.req.TimeRecordDateRangeReq;
import top.aiolife.record.pojo.req.TimeRecordReq;
import top.aiolife.record.pojo.vo.TimeRecordDateRangeVO;
import top.aiolife.record.service.TimeRecordAiFacade;

import java.util.List;

/**
 * 时迹 MCP 工具。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Component
@RequiredArgsConstructor
public class TimeRecordMcpTools {

    private final TimeRecordAiFacade timeRecordAiFacade;

    /**
     * 查询指定日期范围内的时间记录。
     *
     * @param req 日期范围查询请求
     * @return 统一返回结构，data 为时间记录列表
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @McpOperation(
            name = "time_record_queryByDateRange",
            description = "查询指定日期范围内的所有时间记录"
    )
    public ApiResponse<List<TimeRecordDateRangeVO>> queryByDateRangeForAI(TimeRecordDateRangeToolReq req) {
        TimeRecordDateRangeReq rangeReq = new TimeRecordDateRangeReq();
        rangeReq.setStartDate(req.getStartDate());
        rangeReq.setEndDate(req.getEndDate());
        long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(timeRecordAiFacade.queryByDateRangeForAI(rangeReq, userId));
    }

    /**
     * 保存时间记录。
     *
     * @param timeRecordReq 时间记录保存工具请求
     * @return 统一返回结构，data 为是否保存成功
     *
     * @author Ethan
     * @date 2026-05-31
     */
    @McpOperation(
            name = "time_record_save",
            description = "保存时间记录"
    )
    public ApiResponse<Boolean> save(TimeRecordSaveToolReq timeRecordReq) {
        TimeRecordReq saveReq = new TimeRecordReq();
        saveReq.setId(null);
        saveReq.setCategoryId(timeRecordReq.getCategoryId());
        saveReq.setDate(timeRecordReq.getDate());
        saveReq.setStartTime(timeRecordReq.getStartTime());
        saveReq.setEndTime(timeRecordReq.getEndTime());
        saveReq.setTitle(timeRecordReq.getTitle());
        saveReq.setDescription(timeRecordReq.getDescription());
        saveReq.setDuration(null);

        List<ExerciseRecordToolReq> exercises = timeRecordReq.getExercises();
        if (exercises != null) {
            saveReq.setExercises(exercises.stream().map(ex -> {
                ExerciseRecordReq exerciseRecordReq = new ExerciseRecordReq();
                exerciseRecordReq.setId(null);
                exerciseRecordReq.setExerciseTypeId(ex.getExerciseTypeId());
                exerciseRecordReq.setExerciseDate(ex.getExerciseDate());
                exerciseRecordReq.setExerciseCount(ex.getExerciseCount());
                exerciseRecordReq.setDescription(ex.getDescription());
                return exerciseRecordReq;
            }).toList());
        }

        long userId = StpUtil.getLoginIdAsLong();
        timeRecordAiFacade.saveTimeRecord(saveReq, userId, timeRecordReq.getIdempotencyKey());
        return ApiResponse.success(true);
    }
}
