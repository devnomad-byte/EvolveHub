package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpToolGrantInfra;
import org.evolve.domain.resource.model.McpToolGrantEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除 MCP 工具授权业务处理器
 */
@Service
public class DeleteMcpToolGrantManager extends BaseManager<Long, Boolean> {

    @Resource
    private McpToolGrantInfra mcpToolGrantInfra;

    @Override
    protected void check(Long request) {
        if (request == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "授权ID不能为空");
        }
        McpToolGrantEntity existing = mcpToolGrantInfra.getById(request);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "MCP 工具授权不存在");
        }
    }

    @Override
    protected Boolean process(Long request) {
        mcpToolGrantInfra.deleteGrant(request);
        return true;
    }
}
