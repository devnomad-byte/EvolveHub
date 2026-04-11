package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 移除用户角色请求
 *
 * @param userId 用户ID
 * @param roleId 角色ID
 */
public record RemoveUserRoleRequest(
        @NotNull(message = "用户ID不能为空") Long userId,
        @NotNull(message = "角色ID不能为空") Long roleId) {
}
