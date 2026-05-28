package top.aiolife.mcp.api;

import cn.dev33.satoken.stp.StpUtil;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
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
 * 时迹 MCP 工具控制器
 *
 * @author Ethan
 */
@RestController
@RequiredArgsConstructor
public class TimeRecordToolController {

    private final TimeRecordAiFacade timeRecordAiFacade;

    @Tool("查询指定日期范围内的所有时间记录")
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

    @Tool("保存时间记录")
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
