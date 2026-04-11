package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建模型配置请求
 *
 * @param name      模型名称（全局唯一）
 * @param provider  模型提供商（如 OpenAI / Anthropic / DeepSeek）
 * @param apiKey    API密钥
 * @param baseUrl   自定义基址URL（可选）
 * @param enabled   启用状态（1=启用 0=禁用）
 * @param modelType 模型类型（如 chat / embedding）
 */
public record CreateModelConfigRequest(
        @NotBlank(message = "模型名称不能为空") String name,
        @NotBlank(message = "提供商不能为空") String provider,
        @NotBlank(message = "API密钥不能为空") String apiKey,
        String baseUrl,
        @NotNull(message = "启用状态不能为空") Integer enabled,
        String modelType) {
}
