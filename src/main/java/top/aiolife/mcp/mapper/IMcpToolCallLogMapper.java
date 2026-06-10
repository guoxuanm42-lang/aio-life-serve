package top.aiolife.mcp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.mcp.pojo.entity.McpToolCallLogEntity;

/**
 * MCP tool call audit log mapper.
 *
 * <p>Provides basic persistence access to the mcp_tool_call_log table.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Mapper
public interface IMcpToolCallLogMapper extends BaseMapper<McpToolCallLogEntity> {
}
