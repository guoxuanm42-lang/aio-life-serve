package top.aiolife.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.aiolife.ai.pojo.entity.AiAgentConfigEntity;
import top.aiolife.ai.pojo.req.AiAgentConfigSaveReq;
import top.aiolife.ai.pojo.vo.AiAgentConfigVO;

import java.util.List;

/**
 * AI Agent 配置服务接口。
 *
 * @author Ethan
 * @date 2026-06-28
 */
public interface AiAgentConfigService extends IService<AiAgentConfigEntity> {

    /**
     * 查询当前用户可用的 AI Agent 生效配置列表。
     *
     * @param userId 当前登录用户 id
     * @return AI Agent 生效配置列表
     *
     * @author Ethan
     * @date 2026-06-28
     */
    List<AiAgentConfigVO> listEffectiveConfigs(Long userId);

    /**
     * 根据 Agent 编码查询当前用户的生效配置。
     *
     * @param userId 当前登录用户 id
     * @param code Agent 编码
     * @return AI Agent 生效配置
     *
     * @author Ethan
     * @date 2026-06-28
     */
    AiAgentConfigVO getEffectiveConfig(Long userId, String code);

    /**
     * 保存当前用户对指定 AI Agent 的覆盖配置。
     *
     * @param userId 当前登录用户 id
     * @param code Agent 编码
     * @param req 配置保存请求
     * @return 保存后的 AI Agent 生效配置
     *
     * @author Ethan
     * @date 2026-06-28
     */
    AiAgentConfigVO saveUserConfig(Long userId, String code, AiAgentConfigSaveReq req);

    /**
     * 更新当前用户对指定 AI Agent 的启用状态。
     *
     * @param userId 当前登录用户 id
     * @param code Agent 编码
     * @param enabled 是否启用
     * @return 更新后的 AI Agent 生效配置
     *
     * @author Ethan
     * @date 2026-06-28
     */
    AiAgentConfigVO updateStatus(Long userId, String code, Boolean enabled);
}
