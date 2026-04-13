package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 设置全局向量模型请求
 * <p>
 * 系统全局只允许一个向量模型，调用此接口时有则更新、无则创建。
 * </p>
 *
 * @param name     模型名称
 * @param provider 模型提供商（如 OpenAI / DeepSeek）
 * @param apiKey   API 密钥
 * @param baseUrl  自定义基址 URL（可选）
 */
public record SetEmbeddingModelRequest(
        @NotBlank(message = "模型名称不能为空") String name,
        @NotBlank(message = "提供商不能为空") String provider,
        @NotBlank(message = "API密钥不能为空") String apiKey,
        String baseUrl) {
}
