package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.AgentRuntimeConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.AgentRuntimeConfigInfra;
import org.evolve.domain.resource.model.AgentRuntimeConfigEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 获取Agent运行时配置
 */
@Service
public class AgentRuntimeConfigGetManager extends BaseManager<String, List<AgentRuntimeConfigResponse>> {

    @Resource
    private AgentRuntimeConfigInfra agentRuntimeConfigInfra;

    @Override
    protected void check(String configKey) {
        // 配置键为null表示获取所有配置，不做校验
    }

    @Override
    protected List<AgentRuntimeConfigResponse> process(String configKey) {
        List<AgentRuntimeConfigEntity> configs;
        if (configKey == null || configKey.isEmpty()) {
            configs = agentRuntimeConfigInfra.listAll();
        } else {
            AgentRuntimeConfigEntity config = agentRuntimeConfigInfra.getByKey(configKey);
            configs = config != null ? List.of(config) : List.of();
        }
        return configs.stream()
                .map(AgentRuntimeConfigResponse::from)
                .toList();
    }
}
