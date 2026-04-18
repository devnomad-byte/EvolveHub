package org.evolve.admin.response;

import org.evolve.domain.resource.model.ToolGuardConfigEntity;

import java.time.LocalDateTime;

/**
 * 工具守卫配置响应
 */
public record ToolGuardConfigResponse(
        Integer enabled,
        String guardedTools,
        String deniedTools,
        LocalDateTime updateTime
) {
    public static ToolGuardConfigResponse from(ToolGuardConfigEntity entity) {
        return new ToolGuardConfigResponse(
                entity.getEnabled(),
                entity.getGuardedTools(),
                entity.getDeniedTools(),
                entity.getUpdateTime()
        );
    }
}
