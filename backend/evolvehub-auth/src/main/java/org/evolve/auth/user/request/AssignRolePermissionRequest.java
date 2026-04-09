package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotNull;

/**
 * 为角色分配权限请求
 *
 * @param roleId       角色 ID（必填，角色必须存在）
 * @param permissionId 权限 ID（必填，权限必须存在，且该角色尚未持有此权限）
 */
public record AssignRolePermissionRequest(
        @NotNull(message = "角色ID不能为空")
        Long roleId,
        @NotNull(message = "权限ID不能为空")
        Long permissionId) {
}
