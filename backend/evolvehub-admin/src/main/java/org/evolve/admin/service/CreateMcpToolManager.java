package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateMcpToolRequest;
import org.evolve.admin.response.CreateMcpToolResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpToolInfra;
import org.evolve.domain.resource.model.McpToolEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建 MCP 工具业务处理器
 */
@Service
public class CreateMcpToolManager extends BaseManager<CreateMcpToolRequest, CreateMcpToolResponse> {

    @Resource
    private McpToolInfra mcpToolInfra;

    @Override
    protected void check(CreateMcpToolRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "工具名称不能为空");
        }
        if (request.mcpConfigId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "MCP Server ID 不能为空");
        }
    }

    @Override
    protected CreateMcpToolResponse process(CreateMcpToolRequest request) {
        McpToolEntity entity = new McpToolEntity();
        entity.setMcpConfigId(request.mcpConfigId());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setInputSchema(request.inputSchema());
        entity.setRiskLevel(request.riskLevel() != null ? request.riskLevel() : "MEDIUM");
        entity.setToolScope(request.toolScope() != null ? request.toolScope() : "SYSTEM");
        entity.setDeptId(request.deptId());
        entity.setOwnerId(request.ownerId());
        entity.setEnabled(request.enabled() != null ? request.enabled() : 1);
        mcpToolInfra.saveTool(entity);
        return new CreateMcpToolResponse(entity.getId());
    }
}
