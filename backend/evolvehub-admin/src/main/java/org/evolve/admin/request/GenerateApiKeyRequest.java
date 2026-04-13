package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 生成 API Key 请求
 *
 * @param userId 目标用户 ID
 */
public record GenerateApiKeyRequest(
        @NotNull(message = "用户ID不能为空") Long userId) {
}
