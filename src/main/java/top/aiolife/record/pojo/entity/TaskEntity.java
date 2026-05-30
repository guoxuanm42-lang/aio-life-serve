package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代办任务实体，保存清单任务的内容、时间、完成状态、类型与复盘信息。
 *
 * @author Ethan
 * @date 2026-05-30
 */
@Data
@TableName("task")
public class TaskEntity {

    /**
     * 任务 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 任务内容。
     */
    private String content;

    /**
     * 任务备注。
     */
    private String detail;

    /**
     * 兼容旧看板的栏目 ID。
     */
    private Long columnId;

    /**
     * 代办类型 ID，主题跟随类型配置。
     */
    private Long typeId;

    /**
     * 目标完成时间，保留用于兼容旧数据。
     */
    private LocalDateTime dueDate;

    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    private LocalDateTime endTime;

    /**
     * 任务状态：0-未完成，1-已完成，2-已失败。
     */
    private Integer isCompleted;

    /**
     * 失败原因，仅失败状态使用。
     */
    private String failureReason;

    /**
     * 排序值。
     */
    private Integer sortOrder;

    /**
     * 是否删除。
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 类型名称，列表展示使用。
     */
    @TableField(exist = false)
    private String typeName;

    /**
     * 类型所属主题，列表展示与主题筛选使用。
     */
    @TableField(exist = false)
    private String theme;

    /**
     * 类型颜色，列表展示使用。
     */
    @TableField(exist = false)
    private String typeColor;

    /**
     * 类型是否已被逻辑删除，仅用于历史代办展示。
     */
    @TableField(exist = false)
    private Boolean typeDeleted;

    /**
     * 未完成明细数量，兼容旧任务卡片统计。
     */
    @TableField(exist = false)
    private Integer unCompletedCount;
}
