package org.evolve.aiplatform.memory.domain.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 记忆提取请求对象
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryExtractionRequestDTO {

    /*
     * 用户 ID
     */
    private Long userId;

    /*
     * 会话 ID
     */
    private Long sessionId;

    /*
     * 当前会话模型名称
     */
    private String modelName;

    /*
     * 对话全文
     */
    private String conversationContent;

}
