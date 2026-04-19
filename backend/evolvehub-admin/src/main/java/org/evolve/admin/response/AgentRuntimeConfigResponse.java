package org.evolve.admin.response;

import org.evolve.domain.resource.model.AgentRuntimeConfigEntity;

import java.time.LocalDateTime;

/**
 * Agent运行时配置响应
 */
public record AgentRuntimeConfigResponse(
        Long id,
        String configKey,
        String configValue,
        String description,
        LocalDateTime updateTime
) {
    public static AgentRuntimeConfigResponse from(AgentRuntimeConfigEntity entity) {
        return new AgentRuntimeConfigResponse(
                entity.getId(),
                entity.getConfigKey(),
                entity.getConfigValue(),
                entity.getDescription(),
                entity.getUpdateTime()
        );
    }
}
