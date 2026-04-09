package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新权限请求
 * <p>
 * 传 null 的字段不会被更新。修改 permCode 时需保证全局唯一。
 *
 * @param id       权限 ID（必填）
 * @param parentId 父权限 ID
 * @param permName 权限名称
 * @param permCode 权限编码（若传值，需保证全局唯一）
 * @param permType 权限类型（MENU / BUTTON / API）
 * @param path     前端路由路径
 * @param icon     菜单图标
 * @param sort     排序号
 * @param status   状态（0-禁用 1-正常）
 */
public record UpdatePermissionRequest(
        @NotNull(message = "权限ID不能为空")
        Long id,
        Long parentId,
        String permName,
        String permCode,
        String permType,
        String path,
        String icon,
        Integer sort,
        Integer status) {
}
