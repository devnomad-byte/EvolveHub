package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDeptRequest(
        @NotNull(message = "父部门ID不能为空")
        Long parentId,
        @NotBlank(message = "部门名称不能为空")
        String deptName) {
}
