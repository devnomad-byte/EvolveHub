package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotNull;

/**
 * 为用户分配角色请求
 *
 * @param userId 用户 ID（必填，用户必须存在且正常）
 * @param roleId 角色 ID（必填，角色必须存在且正常，且该用户尚未持有此角色）
 */
public record AssignUserRoleRequest(
        @NotNull(message = "用户ID不能为空")
        Long userId,
        @NotNull(message = "角色ID不能为空")
        Long roleId) {
}
