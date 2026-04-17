package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.DeleteMcpToolRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpToolInfra;
import org.evolve.domain.resource.infra.McpToolGrantInfra;
import org.evolve.domain.resource.model.McpToolEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 删除 MCP 工具业务处理器
 */
@Service
public class DeleteMcpToolManager extends BaseManager<DeleteMcpToolRequest, Boolean> {

    @Resource
    private McpToolInfra mcpToolInfra;

    @Resource
    private McpToolGrantInfra mcpToolGrantInfra;

    @Override
    protected void check(DeleteMcpToolRequest request) {
        if (request.id() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "工具ID不能为空");
        }
        McpToolEntity existing = mcpToolInfra.getById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "MCP 工具不存在");
        }
    }

    @Override
    @Transactional
    protected Boolean process(DeleteMcpToolRequest request) {
        // 先删除该工具的所有授权
        mcpToolGrantInfra.deleteByToolId(request.id());
        // 再删除工具
        mcpToolInfra.deleteTool(request.id());
        return true;
    }
}
