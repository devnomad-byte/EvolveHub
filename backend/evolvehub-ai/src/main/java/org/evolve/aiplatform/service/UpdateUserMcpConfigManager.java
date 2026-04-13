package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.request.UpdateUserMcpConfigRequest;
import org.evolve.aiplatform.response.UpdateUserMcpConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.McpConfigInfra;
import org.evolve.common.model.McpConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新用户级 MCP 配置业务处理器
 *
 * @author zhao
 */
@Service
public class UpdateUserMcpConfigManager extends BaseManager<UpdateUserMcpConfigRequest, UpdateUserMcpConfigResponse> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Override
    protected void check(UpdateUserMcpConfigRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        McpConfigEntity existing = mcpConfigInfra.getMcpConfigById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "MCP 配置不存在");
        }
        if (!"USER".equals(existing.getScope()) || !currentUserId.equals(existing.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该 MCP 配置");
        }
    }

    @Override
    protected UpdateUserMcpConfigResponse process(UpdateUserMcpConfigRequest request) {
        McpConfigEntity entity = new McpConfigEntity();
        entity.setId(request.id());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setServerUrl(request.serverUrl());
        entity.setProtocol(request.protocol());
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        mcpConfigInfra.updateMcpConfig(entity);
        return new UpdateUserMcpConfigResponse(request.id());
    }
}
