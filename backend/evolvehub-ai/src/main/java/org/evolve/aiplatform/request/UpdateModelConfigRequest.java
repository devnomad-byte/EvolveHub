package org.evolve.aiplatform.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新模型配置请求
 *
 * @param id        模型配置 ID
 * @param name      模型名称（可选）
 * @param provider  提供商（可选）
 * @param apiKey    API 密钥（可选）
 * @param baseUrl   API 基础 URL（可选）
 * @param enabled   是否启用（可选，0-禁用 1-启用）
 * @param modelType 模型类型（可选）
 */
public record UpdateModelConfigRequest(
        @NotNull(message = "模型配置ID不能为空")
        Long id,
        String name,
        String provider,
        String apiKey,
        String baseUrl,
        Integer enabled,
        String modelType) {
}
