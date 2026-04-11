package org.evolve.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 重置密码请求
 *
 * @author zhao
 */
public record ResetPasswordRequest(

        /**
         * 用户 ID
         */
        Long userId,

        /**
         * 新密码
         */
        @NotBlank(message = "新密码不能为空")
        String newPassword
) {
}
