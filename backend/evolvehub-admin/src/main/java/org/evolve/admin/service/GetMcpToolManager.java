package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.McpToolResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpToolInfra;
import org.evolve.domain.resource.model.McpToolEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 获取 MCP 工具详情业务处理器
 */
@Service
public class GetMcpToolManager extends BaseManager<Long, McpToolResponse> {

    @Resource
    private McpToolInfra mcpToolInfra;

    @Override
    protected void check(Long request) {
        if (request == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "工具ID不能为空");
        }
    }

    @Override
    protected McpToolResponse process(Long request) {
        McpToolEntity tool = mcpToolInfra.getById(request);
        if (tool == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "MCP 工具不存在");
        }
        return toResponse(tool);
    }

    private McpToolResponse toResponse(McpToolEntity tool) {
        McpToolResponse r = new McpToolResponse();
        r.setId(tool.getId());
        r.setMcpConfigId(tool.getMcpConfigId());
        r.setName(tool.getName());
        r.setDescription(tool.getDescription());
        r.setInputSchema(tool.getInputSchema());
        r.setRiskLevel(tool.getRiskLevel());
        r.setToolScope(tool.getToolScope());
        r.setDeptId(tool.getDeptId());
        r.setOwnerId(tool.getOwnerId());
        r.setEnabled(tool.getEnabled());
        return r;
    }
}
