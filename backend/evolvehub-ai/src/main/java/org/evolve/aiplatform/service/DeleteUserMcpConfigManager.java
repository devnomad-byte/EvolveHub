package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.McpConfigInfra;
import org.evolve.common.model.McpConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除用户级 MCP 配置业务处理器
 *
 * @author zhao
 */
@Service
public class DeleteUserMcpConfigManager extends BaseManager<Long, Void> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Override
    protected void check(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        McpConfigEntity existing = mcpConfigInfra.getMcpConfigById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "MCP 配置不存在");
        }
        if (!"USER".equals(existing.getScope()) || !currentUserId.equals(existing.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该 MCP 配置");
        }
    }

    @Override
    protected Void process(Long id) {
        mcpConfigInfra.deleteMcpConfig(id);
        return null;
    }
}
