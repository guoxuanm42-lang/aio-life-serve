package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代办类型实体，保存用户自定义代办分类、所属主题与展示颜色。
 *
 * @author Ethan
 * @date 2026-05-30
 */
@Data
@TableName("task_type")
public class TaskTypeEntity {

    /**
     * 类型 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 类型名称。
     */
    private String name;

    /**
     * 所属主题。
     */
    private String theme;

    /**
     * 展示颜色。
     */
    private String color;

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
}
