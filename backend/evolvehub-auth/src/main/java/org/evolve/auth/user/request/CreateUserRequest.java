package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建用户请求
 *
 * @param username 用户名（登录账号，全局唯一）
 * @param password 密码（明文传入，后端 BCrypt 加密存储）
 * @param nickname 昵称（可选，展示用）
 * @param email    邮箱（可选，全局唯一）
 * @param phone    手机号（可选，全局唯一）
 * @param deptId   所属部门 ID（必填，必须为已存在且正常的部门）
 */
public record CreateUserRequest(
        @NotBlank(message = "用户名不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        String password,
        String nickname,
        String email,
        String phone,
        @NotNull(message = "部门ID不能为空")
        Long deptId) {
}
