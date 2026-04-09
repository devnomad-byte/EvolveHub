package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新角色请求
 * <p>
 * 传 null 的字段不会被更新。若修改 dataScope 从 5 变为其他值，
 * 系统会自动清理 t_role_data_scope 中该角色的自定义部门列表。
 *
 * @param id        角色 ID（必填）
 * @param roleName  角色名称（若传值，需保证全局唯一）
 * @param roleCode  角色编码（若传值，需保证全局唯一）
 * @param dataScope 数据权限范围（1-5，若传值需在合法范围内）
 * @param sort      排序号
 * @param status    状态（0-禁用 1-正常）
 * @param remark    备注
 */
public record UpdateRoleRequest(
        @NotNull(message = "角色ID不能为空")
        Long id,
        String roleName,
        String roleCode,
        Integer dataScope,
        Integer sort,
        Integer status,
        String remark) {
}
