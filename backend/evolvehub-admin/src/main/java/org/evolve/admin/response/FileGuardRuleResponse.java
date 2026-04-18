package org.evolve.admin.response;

/**
 * 文件守卫规则响应
 */
public record FileGuardRuleResponse(
        Long id,
        String ruleId,
        String name,
        String pathPattern,
        String pathType,
        String tools,
        String description,
        String remediation,
        String severity,
        Integer isBuiltin,
        Integer enabled,
        Long createBy,
        String createTime,
        String updateTime
) {}
