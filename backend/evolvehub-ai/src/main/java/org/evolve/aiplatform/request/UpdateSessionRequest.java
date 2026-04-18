package org.evolve.aiplatform.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新对话会话请求
 *
 * @param id            会话 ID
 * @param title         新的会话标题（可选）
 * @param sysPrompt     新的系统提示词（可选）
 * @param modelConfigId 新的模型配置 ID（可选）
 */
public record UpdateSessionRequest(
        @NotNull(message = "会话 ID 不能为空") Long id,
        String title,
        String sysPrompt,
        Long modelConfigId) {
}
