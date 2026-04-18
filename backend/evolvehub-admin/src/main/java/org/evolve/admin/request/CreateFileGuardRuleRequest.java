package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建文件守卫规则请求
 */
public record CreateFileGuardRuleRequest(
        @NotBlank(message = "规则ID不能为空")
        String ruleId,

        @NotBlank(message = "规则名称不能为空")
        String name,

        @NotBlank(message = "路径模式不能为空")
        String pathPattern,

        @NotBlank(message = "路径类型不能为空")
        String pathType,

        String tools,

        String description,

        String remediation,

        String severity,

        Integer enabled
) {}
