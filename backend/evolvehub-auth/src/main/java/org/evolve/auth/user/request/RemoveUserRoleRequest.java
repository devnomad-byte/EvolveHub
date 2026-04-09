package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotNull;

/**
 * 移除用户角色请求
 *
 * @param userId 用户 ID（必填）
 * @param roleId 角色 ID（必填，该用户必须已持有此角色）
 */
public record RemoveUserRoleRequest(
        @NotNull(message = "用户ID不能为空")
        Long userId,
        @NotNull(message = "角色ID不能为空")
        Long roleId) {
}
