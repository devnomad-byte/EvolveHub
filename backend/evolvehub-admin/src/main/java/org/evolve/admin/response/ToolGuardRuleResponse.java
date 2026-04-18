package org.evolve.admin.response;

import org.evolve.domain.resource.model.ToolGuardRuleEntity;

import java.time.LocalDateTime;

/**
 * 工具守卫规则响应
 */
public record ToolGuardRuleResponse(
        Long id,
        String ruleId,
        String name,
        String tools,
        String params,
        String category,
        String severity,
        String patterns,
        String excludePatterns,
        String description,
        String remediation,
        Integer isBuiltin,
        Integer enabled,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
    public static ToolGuardRuleResponse from(ToolGuardRuleEntity entity) {
        return new ToolGuardRuleResponse(
                entity.getId(),
                entity.getRuleId(),
                entity.getName(),
                entity.getTools(),
                entity.getParams(),
                entity.getCategory(),
                entity.getSeverity(),
                entity.getPatterns(),
                entity.getExcludePatterns(),
                entity.getDescription(),
                entity.getRemediation(),
                entity.getIsBuiltin(),
                entity.getEnabled(),
                entity.getCreateTime(),
                entity.getUpdateTime()
        );
    }
}
