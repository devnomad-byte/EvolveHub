package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateMcpConfigRequest;
import org.evolve.admin.response.UpdateMcpConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.McpConfigInfra;
import org.evolve.common.model.McpConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新系统级 MCP 配置业务处理器
 *
 * @author zhao
 */
@Service
public class UpdateMcpConfigManager extends BaseManager<UpdateMcpConfigRequest, UpdateMcpConfigResponse> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Override
    protected void check(UpdateMcpConfigRequest request) {
        McpConfigEntity existing = mcpConfigInfra.getMcpConfigById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "MCP 配置不存在");
        }
        if (request.name() != null && !request.name().isBlank()) {
            McpConfigEntity byName = mcpConfigInfra.getByName(request.name());
            if (byName != null && !byName.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "MCP 服务名称已存在");
            }
        }
    }

    @Override
    protected UpdateMcpConfigResponse process(UpdateMcpConfigRequest request) {
        McpConfigEntity entity = new McpConfigEntity();
        entity.setId(request.id());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setServerUrl(request.serverUrl());
        entity.setProtocol(request.protocol());
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        mcpConfigInfra.updateMcpConfig(entity);
        return new UpdateMcpConfigResponse(request.id());
    }
}
