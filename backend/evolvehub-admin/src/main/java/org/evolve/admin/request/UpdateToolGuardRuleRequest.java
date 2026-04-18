package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新工具守卫规则请求
 */
public record UpdateToolGuardRuleRequest(
        @NotNull(message = "规则ID不能为空")
        Long id,

        String name,

        String tools,

        String params,

        String severity,

        String patterns,

        String excludePatterns,

        String category,

        String description,

        String remediation,

        Integer enabled
) {}
