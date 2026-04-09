package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建角色请求
 *
 * @param roleName  角色名称（全局唯一，如"系统管理员"）
 * @param roleCode  角色编码（全局唯一，如 ROLE_ADMIN，用于代码级判断）
 * @param dataScope 数据权限范围（1-全部 2-本部门及子部门 3-仅本部门 4-仅本人 5-自定义）
 * @param sort      排序号（可选，默认 0）
 * @param remark    备注（可选）
 */
public record CreateRoleRequest(
        @NotBlank(message = "角色名称不能为空")
        String roleName,
        @NotBlank(message = "角色编码不能为空")
        String roleCode,
        @NotNull(message = "数据权限范围不能为空")
        Integer dataScope,
        Integer sort,
        String remark) {
}
