package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 撤销资源授权请求
 *
 * @param userId       被撤销用户 ID
 * @param resourceType 资源类型（MODEL / SKILL / MCP）
 * @param resourceId   资源 ID
 */
public record RevokeResourceGrantRequest(
        @NotNull(message = "用户ID不能为空") Long userId,
        @NotBlank(message = "资源类型不能为空") String resourceType,
        @NotNull(message = "资源ID不能为空") Long resourceId) {
}
