package org.evolve.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 注册请求
 *
 * @param username 用户名（登录账号，全局唯一）
 * @param password 密码（明文传入，后端 BCrypt 加密存储）
 * @param nickname 昵称（可选）
 * @param email    邮箱（可选，全局唯一）
 * @param phone    手机号（可选，全局唯一）
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        String password,
        String nickname,
        String email,
        String phone) {
}
