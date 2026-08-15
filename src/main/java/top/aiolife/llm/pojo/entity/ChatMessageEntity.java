package top.aiolife.llm.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话消息实体，保存消息内容、模型信息及业务来源幂等标识。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Data
@TableName("chat_message")
public class ChatMessageEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long conversationId;

    private String role;

    private String content;

    private String modelName;

    private String sourceType;

    private String idempotencyKey;

    private Long createUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Long updateUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /**
     * 填充消息创建时的通用审计字段。
     *
     * @param userId 当前登录用户 ID
     *
     * @author Ethan
     * @date 2026-08-14
     */
    public void fillCreateCommonField(Long userId) {
        this.createUser = userId;
        this.updateUser = userId;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.isDeleted = 0;
    }
}
