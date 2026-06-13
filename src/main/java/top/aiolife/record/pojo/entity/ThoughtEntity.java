package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

/**
 * 思考（闪念）实体
 *
 * @author Ethan
 */
@Data
@TableName("thought")
public class ThoughtEntity extends BaseEntity {

    private Long userId;

    @JsonAlias({"topic"})
    private String subject;

    private String content;

    @TableField("theme_key")
    private String themeKey;

    @TableField("card_object")
    private String cardObject;

    @TableField("status")
    private String status;

    @TableField("thought_type")
    private String thoughtType;

    @TableField(exist = false)
    private List<ThoughtRelaEventEntity> events;
}
