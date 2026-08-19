package top.aiolife.ai.activity.service;

import top.aiolife.llm.pojo.entity.ChatMessageEntity;
import top.aiolife.llm.pojo.resp.ChatMessageResp;

import java.util.List;

/**
 * 活动总结历史装配服务，批量关联聊天消息与生成时保存的结构化报告快照。
 *
 * @author Ethan
 * @date 2026-08-15
 */
public interface AiActivitySummaryHistoryService {

    /**
     * 将聊天消息转换为历史响应并批量附加当前用户的活动总结快照。
     *
     * @param userId 当前用户 ID
     * @param messages 已完成归属校验并按时间排序的聊天消息
     * @return 保持原顺序的聊天历史响应列表
     *
     * @author Ethan
     * @date 2026-08-15
     */
    List<ChatMessageResp> assemble(Long userId, List<ChatMessageEntity> messages);
}
