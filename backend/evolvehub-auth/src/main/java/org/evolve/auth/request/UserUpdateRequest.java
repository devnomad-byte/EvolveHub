package org.evolve.auth.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新用户请求
 *
 * @author zhao
 */
public record UserUpdateRequest(

        /**
         * 用户 ID
         */
        @NotNull(message = "用户ID不能为空")
        Long id,

        /**
         * 昵称
         */
        String nickname,

        /**
         * 邮箱
         */
        String email,

        /**
         * 手机号
         */
        String phone,

        /**
         * 所属部门 ID
         */
        Long deptId,

        /**
         * 角色 ID
         */
        Long roleId,

        /**
         * 状态（0-禁用 1-正常）
         */
        Integer status
) {
}
