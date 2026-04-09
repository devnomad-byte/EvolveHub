package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新部门请求
 * <p>
 * 传 null 的字段不会被更新。修改 parentId 时不允许将自身设为自己的子部门（防止循环引用）。
 *
 * @param id       部门 ID（必填）
 * @param parentId 父部门 ID（若传值，不可为自身 ID）
 * @param deptName 部门名称（若传值，同级下唯一）
 * @param sort     排序号
 * @param status   状态（0-禁用 1-正常）
 */
public record UpdateDeptRequest(
        @NotNull(message = "部门ID不能为空")
        Long id,
        Long parentId,
        String deptName,
        Integer sort,
        Integer status) {
}
