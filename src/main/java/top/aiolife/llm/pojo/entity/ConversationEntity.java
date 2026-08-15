package top.aiolife.llm.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversation")
/**
 * AI 对话会话实体，记录会话所属用户、绑定助手与展示信息。
 *
 * @author Ethan
 * @date 2026-07-20
 */
public class ConversationEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String title;

    private String agentCode;

    private Long createUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Long updateUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /**
     * 填充新会话的通用创建字段。
     *
     * @param userId 当前登录用户 id
     *
     * @author Ethan
     * @date 2026-07-20
     */
    public void fillCreateCommonField(Long userId) {
        this.userId = userId;
        this.createUser = userId;
        this.updateUser = userId;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.isDeleted = 0;
    }
}
