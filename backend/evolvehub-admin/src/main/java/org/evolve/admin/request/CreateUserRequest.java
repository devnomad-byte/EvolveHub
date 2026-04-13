package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建用户请求
 *
 * @param username 用户名（全局唯一）
 * @param password 登录密码（存储时使用 BCrypt 加密）
 * @param nickname 昵称
 * @param email    邮箱（全局唯一，可选）
 * @param phone    手机号（全局唯一，可选）
 * @param deptId   所属部门ID（部门必须存在）
 * @param roleId   角色ID（必须）
 * @param status   状态（0-禁用 1-正常，默认1）
 */
public record CreateUserRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        String nickname,
        String email,
        String phone,
        @NotNull(message = "部门ID不能为空") Long deptId,
        @NotNull(message = "角色ID不能为空") Long roleId,
        Integer status
) {
}
