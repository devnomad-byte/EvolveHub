package org.evolve.admin.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 设置角色数据范围请求
 *
 * @param roleId  角色ID
 * @param deptIds 自定义数据范围的部门ID列表
 */
public record AssignRoleDataScopeRequest(
        @NotNull(message = "角色ID不能为空") Long roleId,
        @NotEmpty(message = "部门ID列表不能为空") List<Long> deptIds) {
}
