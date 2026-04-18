package org.evolve.admin.response;

import org.evolve.domain.resource.model.ToolGuardHistoryEntity;

import java.time.LocalDateTime;

/**
 * 工具守卫历史响应
 */
public record ToolGuardHistoryResponse(
        Long id,
        String sessionId,
        Long userId,
        String userNickname,
        String toolName,
        String paramName,
        String matchedRuleId,
        String matchedValue,
        String severity,
        String action,
        LocalDateTime createTime
) {
    public static ToolGuardHistoryResponse from(ToolGuardHistoryEntity entity) {
        return new ToolGuardHistoryResponse(
                entity.getId(),
                entity.getSessionId(),
                entity.getUserId(),
                entity.getUserNickname(),
                entity.getToolName(),
                entity.getParamName(),
                entity.getMatchedRuleId(),
                entity.getMatchedValue(),
                entity.getSeverity(),
                entity.getAction(),
                entity.getCreateTime()
        );
    }
}
