package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.ToolGuardConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ToolGuardConfigInfra;
import org.evolve.domain.resource.model.ToolGuardConfigEntity;
import org.springframework.stereotype.Service;

/**
 * 获取工具守卫配置
 */
@Service
public class ToolGuardConfigGetManager extends BaseManager<Void, ToolGuardConfigResponse> {

    @Resource
    private ToolGuardConfigInfra toolGuardConfigInfra;

    @Override
    protected void check(Void request) {}

    @Override
    protected ToolGuardConfigResponse process(Void request) {
        ToolGuardConfigEntity config = toolGuardConfigInfra.getConfig();
        if (config == null) {
            // 返回默认值
            return new ToolGuardConfigResponse(1, "[\"execute_shell_command\"]", "[]", null);
        }
        return ToolGuardConfigResponse.from(config);
    }
}
