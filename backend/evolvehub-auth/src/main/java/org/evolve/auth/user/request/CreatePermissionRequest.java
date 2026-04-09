package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建权限请求
 *
 * @param parentId 父权限 ID（0 表示顶级权限）
 * @param permName 权限名称（展示用，如"用户管理"）
 * @param permCode 权限编码（全局唯一，如 user:list、kb:read，用于鉴权）
 * @param permType 权限类型（MENU-菜单 / BUTTON-按钮 / API-接口）
 * @param path     前端路由路径（仅 MENU 类型有效，可选）
 * @param icon     菜单图标（仅 MENU 类型有效，可选）
 * @param sort     排序号（可选，默认 0）
 */
public record CreatePermissionRequest(
        @NotNull(message = "父权限ID不能为空")
        Long parentId,
        @NotBlank(message = "权限名称不能为空")
        String permName,
        @NotBlank(message = "权限编码不能为空")
        String permCode,
        @NotBlank(message = "权限类型不能为空")
        String permType,
        String path,
        String icon,
        Integer sort) {
}
