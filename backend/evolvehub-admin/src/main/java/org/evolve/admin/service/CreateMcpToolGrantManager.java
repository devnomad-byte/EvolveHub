package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateMcpToolGrantRequest;
import org.evolve.admin.response.CreateMcpToolGrantResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpToolGrantInfra;
import org.evolve.domain.resource.infra.McpToolInfra;
import org.evolve.domain.resource.model.McpToolEntity;
import org.evolve.domain.resource.model.McpToolGrantEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建 MCP 工具授权业务处理器
 */
@Service
public class CreateMcpToolGrantManager extends BaseManager<CreateMcpToolGrantRequest, CreateMcpToolGrantResponse> {

    @Resource
    private McpToolGrantInfra mcpToolGrantInfra;

    @Resource
    private McpToolInfra mcpToolInfra;

    @Override
    protected void check(CreateMcpToolGrantRequest request) {
        if (request.toolId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "工具ID不能为空");
        }
        if (request.grantType() == null || request.grantType().isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "授权类型不能为空");
        }
        if (request.targetId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "目标ID不能为空");
        }
        // 检查工具是否存在
        McpToolEntity tool = mcpToolInfra.getById(request.toolId());
        if (tool == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "MCP 工具不存在");
        }
        // 检查授权类型是否有效
        if (!request.grantType().matches("USER|DEPT|ROLE")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "授权类型必须是 USER、DEPT 或 ROLE");
        }
    }

    @Override
    protected CreateMcpToolGrantResponse process(CreateMcpToolGrantRequest request) {
        McpToolGrantEntity entity = new McpToolGrantEntity();
        entity.setToolId(request.toolId());
        entity.setGrantType(request.grantType());
        entity.setTargetId(request.targetId());
        mcpToolGrantInfra.saveGrant(entity);
        return new CreateMcpToolGrantResponse(entity.getId());
    }
}
