package org.evolve.admin.response;

import org.evolve.domain.resource.model.AgentRuntimeConfigHistoryEntity;

import java.time.LocalDateTime;

/**
 * Agent运行时配置历史响应
 */
public record AgentRuntimeConfigHistoryResponse(
        Long id,
        Long operatorId,
        String operatorName,
        String configKey,
        String oldValue,
        String newValue,
        String changeReason,
        LocalDateTime createTime
) {
    public static AgentRuntimeConfigHistoryResponse from(AgentRuntimeConfigHistoryEntity entity) {
        return new AgentRuntimeConfigHistoryResponse(
                entity.getId(),
                entity.getOperatorId(),
                entity.getOperatorName(),
                entity.getConfigKey(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getChangeReason(),
                entity.getCreateTime()
        );
    }
}
