package org.evolve.aiplatform.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新用户级模型配置请求
 *
 * @param id        模型配置 ID（必填）
 * @param name      模型名称
 * @param provider  模型提供商
 * @param apiKey    API 密钥
 * @param baseUrl   自定义基址 URL
 * @param enabled   启用状态（1=启用 0=禁用）
 * @param modelType 模型类型
 */
public record UpdateUserModelConfigRequest(
        @NotNull(message = "模型配置ID不能为空") Long id,
        String name, String provider, String apiKey, String baseUrl,
        Integer enabled, String modelType) {
}
