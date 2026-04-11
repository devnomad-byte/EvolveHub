package org.evolve.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 更新部门请求
 *
 * @author zhao
 */
public record DeptUpdateRequest(

        /**
         * 部门 ID
         */
        @NotNull(message = "部门ID不能为空")
        Long id,

        /**
         * 部门名称
         */
        @NotBlank(message = "部门名称不能为空")
        String deptName,

        /**
         * 上级部门 ID（0 表示根部门）
         */
        Long parentId,

        /**
         * 排序号
         */
        Integer sort,

        /**
         * 状态（0-禁用 1-正常）
         */
        Integer status
) {
}
