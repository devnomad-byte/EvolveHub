package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新角色请求
 *
 * @param id        角色ID（必填）
 * @param roleName  角色名称（全局唯一）
 * @param roleCode  角色编码（全局唯一）
 * @param dataScope 数据权限范围（1=全部 2=本部门 3=本部门及以下 4=仅本人 5=自定义部门）
 * @param sort      排序号
 * @param status    状态（1=正常 0=停用）
 * @param remark    备注
 */
public record UpdateRoleRequest(
        @NotNull(message = "角色ID不能为空") Long id,
        String roleName, String roleCode, Integer dataScope,
        Integer sort, Integer status, String remark) {
}
