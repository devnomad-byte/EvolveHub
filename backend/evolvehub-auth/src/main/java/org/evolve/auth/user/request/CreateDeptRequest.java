package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建部门请求
 *
 * @param parentId 父部门 ID（0 表示顶级部门，非 0 时父部门必须存在）
 * @param deptName 部门名称（同一父部门下唯一）
 * @param sort     排序号（可选，默认 0）
 */
public record CreateDeptRequest(
        @NotNull(message = "父部门ID不能为空")
        Long parentId,
        @NotBlank(message = "部门名称不能为空")
        String deptName,
        Integer sort) {
}
