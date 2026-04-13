package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.request.CreateUserMcpConfigRequest;
import org.evolve.aiplatform.response.CreateUserMcpConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpConfigInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.springframework.stereotype.Service;

/**
 * 创建用户级 MCP 配置业务处理器
 *
 * @author zhao
 */
@Service
public class CreateUserMcpConfigManager extends BaseManager<CreateUserMcpConfigRequest, CreateUserMcpConfigResponse> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Override
    protected void check(CreateUserMcpConfigRequest request) {
    }

    @Override
    protected CreateUserMcpConfigResponse process(CreateUserMcpConfigRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        McpConfigEntity entity = new McpConfigEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setServerUrl(request.serverUrl());
        entity.setProtocol(request.protocol() != null ? request.protocol() : "stdio");
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        entity.setScope("USER");
        entity.setOwnerId(currentUserId);
        mcpConfigInfra.createMcpConfig(entity);
        return new CreateUserMcpConfigResponse(entity.getId());
    }
}
