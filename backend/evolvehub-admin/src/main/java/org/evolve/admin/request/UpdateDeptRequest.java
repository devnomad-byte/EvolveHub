package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新部门请求
 *
 * @param id       部门ID（必填）
 * @param parentId 父部门ID（0 表示顶级部门，非 0 时父部门必须存在）
 * @param deptName 部门名称（同一父部门下唯一）
 * @param sort     排序号
 * @param status   状态（1=正常 0=停用）
 */
public record UpdateDeptRequest(
        @NotNull(message = "部门ID不能为空") Long id,
        Long parentId, String deptName, Integer sort, Integer status) {
}
