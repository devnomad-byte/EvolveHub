package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateMcpToolRequest;
import org.evolve.admin.response.UpdateMcpToolResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpToolInfra;
import org.evolve.domain.resource.model.McpToolEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新 MCP 工具业务处理器
 */
@Service
public class UpdateMcpToolManager extends BaseManager<UpdateMcpToolRequest, UpdateMcpToolResponse> {

    @Resource
    private McpToolInfra mcpToolInfra;

    @Override
    protected void check(UpdateMcpToolRequest request) {
        if (request.id() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "工具ID不能为空");
        }
        McpToolEntity existing = mcpToolInfra.getById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "MCP 工具不存在");
        }
    }

    @Override
    protected UpdateMcpToolResponse process(UpdateMcpToolRequest request) {
        McpToolEntity entity = new McpToolEntity();
        entity.setId(request.id());
        if (request.name() != null) entity.setName(request.name());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.inputSchema() != null) entity.setInputSchema(request.inputSchema());
        if (request.riskLevel() != null) entity.setRiskLevel(request.riskLevel());
        if (request.toolScope() != null) entity.setToolScope(request.toolScope());
        if (request.deptId() != null) entity.setDeptId(request.deptId());
        if (request.ownerId() != null) entity.setOwnerId(request.ownerId());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
        mcpToolInfra.updateTool(entity);
        return new UpdateMcpToolResponse(request.id());
    }
}
