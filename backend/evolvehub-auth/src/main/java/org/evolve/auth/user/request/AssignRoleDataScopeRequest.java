package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 设置角色自定义数据范围请求
 * <p>
 * 仅当角色的 dataScope = 5（自定义）时才允许调用。
 * 执行时会先清除该角色旧的自定义部门列表，再写入新列表（全量替换）。
 *
 * @param roleId  角色 ID（必填，角色 dataScope 必须为 5）
 * @param deptIds 可见部门 ID 列表（不能为空，每个部门必须存在）
 */
public record AssignRoleDataScopeRequest(
        @NotNull(message = "角色ID不能为空")
        Long roleId,
        @NotEmpty(message = "部门ID列表不能为空")
        List<Long> deptIds) {
}
