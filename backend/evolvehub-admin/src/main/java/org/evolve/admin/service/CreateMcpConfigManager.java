package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateMcpConfigRequest;
import org.evolve.admin.response.CreateMcpConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpConfigInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import cn.dev33.satoken.stp.StpUtil;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建 MCP 配置业务处理器
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
        // DEPT scope 时 deptId 不能为空
        if ("DEPT".equals(request.scope()) && request.deptId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门级 MCP 必须指定部门");
        }
    }

    @Override
    protected CreateMcpConfigResponse process(CreateMcpConfigRequest request) {
        McpConfigEntity entity = new McpConfigEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setTransportType(request.transportType() != null ? request.transportType() : "REMOTE");
        entity.setServerUrl(request.serverUrl());
        entity.setPackagePath(request.packagePath());
        entity.setCommand(request.command());
        entity.setArgs(request.args());
        entity.setEnv(request.env());
        entity.setProtocol(request.protocol() != null ? request.protocol() : "SSE");
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        entity.setScope(request.scope() != null ? request.scope() : "SYSTEM");
        entity.setDeptId(request.deptId());
        // USER scope 时 ownerId 通过 SecurityContext 获取
        entity.setOwnerId("USER".equals(request.scope()) ? getCurrentUserId() : null);
        mcpConfigInfra.createMcpConfig(entity);
        return new CreateMcpConfigResponse(entity.getId());
    }

    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
