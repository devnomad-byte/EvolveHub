package org.evolve.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 初始化超级管理员请求
 *
 * @author zhao
 */
public record InitAdminRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度为 3-20 个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度为 6-20 个字符")
        String password,

        @NotBlank(message = "昵称不能为空")
        @Size(max = 20, message = "昵称最多 20 个字符")
        String nickname,

        String initSecretKey // 可选的初始化密钥
) {
}
