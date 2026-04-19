package org.evolve.admin.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

/**
 * 批量更新Agent运行时配置请求
 */
public record BatchUpdateAgentRuntimeConfigRequest(
        @NotEmpty(message = "配置不能为空")
        Map<String, String> configs,

        String changeReason
) {}
