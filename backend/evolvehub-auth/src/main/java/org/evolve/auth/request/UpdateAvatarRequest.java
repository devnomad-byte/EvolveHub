package org.evolve.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新头像请求
 *
 * @author zhao
 */
public record UpdateAvatarRequest(

        /**
         * 头像 Base64 数据（含 data:image/xxx;base64, 前缀）
         */
        @NotBlank(message = "头像数据不能为空")
        String avatar
) {
}
