package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 创建工具守卫规则请求
 */
public record CreateToolGuardRuleRequest(
        @NotBlank(message = "规则ID不能为空")
        @Pattern(regexp = "^[A-Za-z_][A-Za-z0-9_]*$", message = "规则ID格式不正确")
        String ruleId,

        @NotBlank(message = "规则名称不能为空")
        String name,

        @NotBlank(message = "工具列表不能为空")
        String tools,

        @NotBlank(message = "参数列表不能为空")
        String params,

        @NotBlank(message = "严重级别不能为空")
        String severity,

        @NotBlank(message = "正则表达式不能为空")
        String patterns,

        String excludePatterns,

        String category,

        String description,

        String remediation,

        Integer enabled
) {}
