package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 更新环境变量请求
 */
public record UpdateEnvVarRequest(
        @NotNull(message = "ID 不能为空")
        Long id,

        @NotBlank(message = "变量名不能为空")
        @Pattern(regexp = "^[A-Za-z_][A-Za-z0-9_]*$", message = "变量名格式不正确，应以字母或下划线开头")
        String varKey,

        @NotBlank(message = "变量值不能为空")
        String varValue,

        String varGroup,

        String description,

        Integer isSensitive,

        Integer status,

        Integer sort
) {}
