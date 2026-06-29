package top.aiolife.ai.memory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.ai.memory.pojo.entity.AiMemoryEntity;
import top.aiolife.ai.memory.pojo.req.AiMemorySaveReq;
import top.aiolife.ai.memory.pojo.vo.AiMemoryVO;

import java.util.List;

/**
 * AI 长期记忆服务接口。
 *
 * @author Ethan
 * @date 2026-06-28
 */
public interface AiMemoryService extends IService<AiMemoryEntity> {

    /**
     * 查询当前用户的长期记忆列表。
     *
     * @param userId 当前登录用户 id
     * @param agentCode Agent 编码，可为空
     * @param memoryType 记忆类型，可为空
     * @return 长期记忆列表
     *
     * @author Ethan
     * @date 2026-06-28
     */
    List<AiMemoryVO> listMemories(Long userId, String agentCode, String memoryType);

    /**
     * 查询当前用户的单条长期记忆。
     *
     * @param userId 当前登录用户 id
     * @param id 记忆 id
     * @return 长期记忆视图对象
     *
     * @author Ethan
     * @date 2026-06-28
     */
    AiMemoryVO getMemory(Long userId, Long id);

    /**
     * 保存当前用户的长期记忆。
     *
     * @param userId 当前登录用户 id
     * @param req 长期记忆保存请求
     * @return 保存后的长期记忆
     *
     * @author Ethan
     * @date 2026-06-28
     */
    AiMemoryVO saveMemory(Long userId, AiMemorySaveReq req);

    /**
     * 更新当前用户的长期记忆。
     *
     * @param userId 当前登录用户 id
     * @param id 记忆 id
     * @param req 长期记忆保存请求
     * @return 更新后的长期记忆
     *
     * @author Ethan
     * @date 2026-06-28
     */
    AiMemoryVO updateMemory(Long userId, Long id, AiMemorySaveReq req);

    /**
     * 更新当前用户长期记忆的启用状态。
     *
     * @param userId 当前登录用户 id
     * @param id 记忆 id
     * @param enabled 是否启用
     * @return 更新后的长期记忆
     *
     * @author Ethan
     * @date 2026-06-28
     */
    AiMemoryVO updateStatus(Long userId, Long id, Boolean enabled);

    /**
     * 查询聊天时可注入的长期记忆。
     *
     * @param userId 当前登录用户 id
     * @param agentCode Agent 编码
     * @param maxMemoryItems 最大记忆条数
     * @return 可注入的长期记忆列表
     *
     * @author Ethan
     * @date 2026-06-28
     */
    List<AiMemoryVO> listEffectiveMemories(Long userId, String agentCode, Integer maxMemoryItems);
}
