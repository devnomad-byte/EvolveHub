package org.evolve.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码请求
 *
 * @author zhao
 */
public record PasswordChangeRequest(

        /**
         * 旧密码
         */
        @NotBlank(message = "旧密码不能为空")
        String oldPassword,

        /**
         * 新密码
         */
        @NotBlank(message = "新密码不能为空")
        String newPassword
) {
}
