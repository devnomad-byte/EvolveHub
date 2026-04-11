package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新权限请求
 *
 * @param id       权限ID（必填）
 * @param parentId 父权限ID（0 表示顶级）
 * @param permName 权限名称
 * @param permCode 权限编码（全局唯一）
 * @param permType 权限类型（MENU/BUTTON/API）
 * @param path     前端路由或后端API路径
 * @param icon     图标
 * @param sort     排序号
 * @param status   状态（1=正常 0=停用）
 */
public record UpdatePermissionRequest(
        @NotNull(message = "权限ID不能为空") Long id,
        Long parentId, String permName, String permCode, String permType,
        String path, String icon, Integer sort, Integer status) {
}
