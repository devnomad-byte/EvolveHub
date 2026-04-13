package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.McpConfigInfra;
import org.evolve.common.model.McpConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询单个 MCP 配置业务处理器
 *
 * @author zhao
 */
@Service
public class GetMcpConfigManager extends BaseManager<Long, McpConfigEntity> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Override
    protected void check(Long id) {
        if (mcpConfigInfra.getMcpConfigById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "MCP 配置不存在");
        }
    }

    @Override
    protected McpConfigEntity process(Long id) {
        return mcpConfigInfra.getMcpConfigById(id);
    }
}
