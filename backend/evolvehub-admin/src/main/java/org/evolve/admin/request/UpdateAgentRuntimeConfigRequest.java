package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新Agent运行时配置请求
 */
public record UpdateAgentRuntimeConfigRequest(
        @NotBlank(message = "配置键不能为空")
        String configKey,

        @NotBlank(message = "配置值不能为空")
        String configValue,

        String changeReason
) {}
