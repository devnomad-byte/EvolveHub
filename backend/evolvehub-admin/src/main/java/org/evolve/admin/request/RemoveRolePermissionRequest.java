package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 移除角色权限请求
 *
 * @param roleId       角色ID
 * @param permissionId 权限ID
 */
public record RemoveRolePermissionRequest(
        @NotNull(message = "角色ID不能为空") Long roleId,
        @NotNull(message = "权限ID不能为空") Long permissionId) {
}
