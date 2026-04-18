package org.evolve.admin.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 更新安全扫描配置请求
 */
public record UpdateSecurityScannerConfigRequest(
        @NotNull(message = "启用状态不能为空")
        Integer enabled,

        @NotNull(message = "扫描模式不能为空")
        @Pattern(regexp = "^(block|warn|off)$", message = "扫描模式必须是 block/warn/off 之一")
        String mode,

        @NotNull(message = "超时时间不能为空")
        @Min(value = 1, message = "超时时间最小为1秒")
        @Max(value = 300, message = "超时时间最大为300秒")
        Integer timeout
) {}
