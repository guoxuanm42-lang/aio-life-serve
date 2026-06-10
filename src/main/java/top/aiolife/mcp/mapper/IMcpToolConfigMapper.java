package top.aiolife.mcp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.mcp.pojo.entity.McpToolConfigEntity;

/**
 * MCP tool configuration mapper.
 *
 * <p>Provides basic persistence access to the mcp_tool_config table.</p>
 *
 * @author Ethan
 * @date 2026-06-03
 */
@Mapper
public interface IMcpToolConfigMapper extends BaseMapper<McpToolConfigEntity> {
}
