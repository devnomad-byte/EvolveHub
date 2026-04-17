package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateMcpConfigRequest;
import org.evolve.admin.response.UpdateMcpConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpConfigInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import cn.dev33.satoken.stp.StpUtil;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新 MCP 配置业务处理器
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
        // DEPT scope 时 deptId 不能为空
        if (request.scope() != null && "DEPT".equals(request.scope()) && request.deptId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门级 MCP 必须指定部门");
        }
    }

    @Override
    protected UpdateMcpConfigResponse process(UpdateMcpConfigRequest request) {
        McpConfigEntity entity = new McpConfigEntity();
        entity.setId(request.id());
        if (request.name() != null) entity.setName(request.name());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.transportType() != null) entity.setTransportType(request.transportType());
        if (request.serverUrl() != null) entity.setServerUrl(request.serverUrl());
        if (request.packagePath() != null) entity.setPackagePath(request.packagePath());
        if (request.command() != null) entity.setCommand(request.command());
        if (request.args() != null) entity.setArgs(request.args());
        if (request.env() != null) entity.setEnv(request.env());
        if (request.protocol() != null) entity.setProtocol(request.protocol());
        if (request.config() != null) entity.setConfig(request.config());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
        if (request.scope() != null) entity.setScope(request.scope());
        if (request.deptId() != null) entity.setDeptId(request.deptId());
        // scope 变更时同步更新 ownerId
        if (request.scope() != null) {
            if ("USER".equals(request.scope())) {
                entity.setOwnerId(StpUtil.getLoginIdAsLong());
            } else {
                entity.setOwnerId(null);
            }
        }
        mcpConfigInfra.updateMcpConfig(entity);
        return new UpdateMcpConfigResponse(request.id());
    }
}
