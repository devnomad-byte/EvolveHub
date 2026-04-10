package org.evolve.aiplatform.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建模型配置请求
 *
 * @param name      模型名称（全局唯一）
 * @param provider  提供商（如 openai、anthropic、zhipu 等）
 * @param apiKey    API 密钥
 * @param baseUrl   API 基础 URL
 * @param enabled   是否启用（0-禁用 1-启用）
 * @param modelType 模型类型（如 chat、embedding、image 等）
 */
public record CreateModelConfigRequest(
        @NotBlank(message = "模型名称不能为空")
        String name,
        @NotBlank(message = "提供商不能为空")
        String provider,
        @NotBlank(message = "API密钥不能为空")
        String apiKey,
        String baseUrl,
        @NotNull(message = "启用状态不能为空")
        Integer enabled,
        String modelType) {
}
