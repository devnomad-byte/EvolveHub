package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateToolGuardConfigRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ToolGuardConfigInfra;
import org.springframework.stereotype.Service;

/**
 * 更新工具守卫配置
 */
@Service
public class ToolGuardConfigUpdateManager extends BaseManager<UpdateToolGuardConfigRequest, Void> {

    @Resource
    private ToolGuardConfigInfra toolGuardConfigInfra;

    @Override
    protected void check(UpdateToolGuardConfigRequest request) {
        // 校验 enabled 值
        if (request.enabled() != 0 && request.enabled() != 1) {
            throw new org.evolve.common.web.exception.BusinessException(
                org.evolve.common.web.response.ResultCode.BAD_REQUEST, "无效的启用状态");
        }
    }

    @Override
    protected Void process(UpdateToolGuardConfigRequest request) {
        toolGuardConfigInfra.updateConfig(request.guardedTools(), request.deniedTools());
        toolGuardConfigInfra.updateEnabled(request.enabled());
        return null;
    }
}
