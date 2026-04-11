package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建权限请求
 *
 * @param parentId 父权限ID（0 表示顶级权限）
 * @param permName 权限名称
 * @param permCode 权限编码（全局唯一）
 * @param permType 权限类型（MENU/BUTTON/API）
 * @param path     前端路由或后端API路径
 * @param icon     图标
 * @param sort     排序号（可选，默认 0）
 */
public record CreatePermissionRequest(
        @NotNull(message = "父权限ID不能为空") Long parentId,
        @NotBlank(message = "权限名称不能为空") String permName,
        @NotBlank(message = "权限编码不能为空") String permCode,
        @NotBlank(message = "权限类型不能为空") String permType,
        String path, String icon, Integer sort) {
}
