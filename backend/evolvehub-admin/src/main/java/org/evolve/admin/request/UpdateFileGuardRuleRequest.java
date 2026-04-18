package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新文件守卫规则请求
 */
public record UpdateFileGuardRuleRequest(
        @NotNull(message = "规则ID不能为空")
        Long id,

        String name,

        String pathPattern,

        String pathType,

        String tools,

        String description,

        String remediation,

        String severity,

        Integer enabled
) {}
