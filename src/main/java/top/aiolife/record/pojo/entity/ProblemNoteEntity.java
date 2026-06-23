package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目记录实体，保存用户手动录入的题目、Java 解法代码和思路备注。
 *
 * @author Ethan
 * @date 2026-06-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("problem_note")
public class ProblemNoteEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 分类 ID，空值表示未分类。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long categoryId;

    /**
     * 题目标题。
     */
    private String title;

    /**
     * 题目内容。
     */
    private String problemContent;

    /**
     * Java 解法代码。
     */
    private String solutionCode;

    /**
     * 解题思路备注。
     */
    private String ideaNote;

    /**
     * 题目难度。
     */
    private String difficulty;

    /**
     * 标签，第一阶段用逗号分隔字符串保存。
     */
    private String tags;

    /**
     * 状态：draft/solved/reviewing/archived。
     */
    private String status;
}
