package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新工具守卫配置请求
 */
public record UpdateToolGuardConfigRequest(
        @NotNull(message = "启用状态不能为空")
        Integer enabled,

        String guardedTools,

        String deniedTools
) {}
