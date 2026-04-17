package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.McpToolResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpToolInfra;
import org.evolve.domain.resource.model.McpToolEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分页查询 MCP 工具列表业务处理器
 */
@Service
public class ListMcpToolManager extends BaseManager<PageRequest, PageResponse<McpToolResponse>> {

    @Resource
    private McpToolInfra mcpToolInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<McpToolResponse> process(PageRequest request) {
        var page = mcpToolInfra.listPage(request.pageNum(), request.pageSize());
        List<McpToolResponse> records = page.getRecords().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(records, page.getTotal(), request.pageNum(), request.pageSize());
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
