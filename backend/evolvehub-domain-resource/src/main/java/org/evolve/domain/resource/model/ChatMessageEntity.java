package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 对话消息实体
 *
 * @author zhao
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName(value = "eh_chat_message", autoResultMap = true)
public class ChatMessageEntity extends BaseEntity {

    private Long sessionId;
    private String role;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String toolCalls;
    private String toolCallId;
    private String modelName;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private String finishReason;
    private Integer durationMs;
}
