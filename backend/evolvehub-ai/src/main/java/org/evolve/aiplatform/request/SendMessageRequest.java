package org.evolve.aiplatform.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送对话消息请求
 *
 * @param sessionId     会话 ID（为空则自动创建新会话）
 * @param modelConfigId 模型配置 ID（新建会话时必传，已有会话时可不传）
 * @param sysPrompt     自定义系统提示词（可选，仅新建会话时生效）
 * @param content       用户输入的消息内容
 */
public record SendMessageRequest(
        Long sessionId,
        Long modelConfigId,
        String sysPrompt,
        @NotBlank(message = "消息内容不能为空") String content) {
}
