package org.evolve.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建用户请求
 *
 * @author zhao
 */
public record UserCreateRequest(

        /**
         * 用户名（登录账号，全局唯一）
         */
        @NotBlank(message = "用户名不能为空")
        String username,

        /**
         * 密码
         */
        @NotBlank(message = "密码不能为空")
        String password,

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
        @NotNull(message = "部门不能为空")
        Long deptId,

        /**
         * 角色 ID 列表
         */
        @NotNull(message = "角色不能为空")
        Long roleId,

        /**
         * 状态（0-禁用 1-正常）
         */
        Integer status
) {
}
