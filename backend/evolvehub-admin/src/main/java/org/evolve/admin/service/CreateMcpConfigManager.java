package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateMcpConfigRequest;
import org.evolve.admin.response.CreateMcpConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpConfigInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建系统级 MCP 配置业务处理器
 *
 * @author zhao
 */
@Service
public class CreateMcpConfigManager extends BaseManager<CreateMcpConfigRequest, CreateMcpConfigResponse> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Override
    protected void check(CreateMcpConfigRequest request) {
        if (mcpConfigInfra.getByName(request.name()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "MCP 服务名称已存在");
        }
    }

    @Override
    protected CreateMcpConfigResponse process(CreateMcpConfigRequest request) {
        McpConfigEntity entity = new McpConfigEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setServerUrl(request.serverUrl());
        entity.setProtocol(request.protocol() != null ? request.protocol() : "stdio");
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        entity.setScope("SYSTEM");
        entity.setOwnerId(null);
        mcpConfigInfra.createMcpConfig(entity);
        return new CreateMcpConfigResponse(entity.getId());
    }
}
