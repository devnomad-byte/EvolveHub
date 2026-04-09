package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotNull;

/**
 * 移除角色权限请求
 *
 * @param roleId       角色 ID（必填）
 * @param permissionId 权限 ID（必填，该角色必须已持有此权限）
 */
public record RemoveRolePermissionRequest(
        @NotNull(message = "角色ID不能为空")
        Long roleId,
        @NotNull(message = "权限ID不能为空")
        Long permissionId) {
}
