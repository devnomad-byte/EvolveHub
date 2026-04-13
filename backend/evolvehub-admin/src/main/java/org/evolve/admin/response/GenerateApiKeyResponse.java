package org.evolve.admin.response;

/**
 * 生成 API Key 响应
 *
 * @param id     API Key 记录 ID
 * @param userId 绑定的用户 ID
 * @param apiKey 生成的密钥值
 */
public record GenerateApiKeyResponse(Long id, Long userId, String apiKey) {
}
