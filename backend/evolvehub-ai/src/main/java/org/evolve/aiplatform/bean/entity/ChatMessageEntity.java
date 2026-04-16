package org.evolve.aiplatform.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 对话消息实体
 * <p>
 * 对应数据库表 eh_chat_message，记录每条对话消息。
 * 支持 user / assistant / system / tool 四种角色，
 * assistant 消息可携带 tool_calls（工具调用），tool 消息通过 toolCallId 关联。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName(value = "eh_chat_message", autoResultMap = true)
public class ChatMessageEntity extends BaseEntity {

    /**
     * 所属会话 ID
     */
    private Long sessionId;

    /**
     * 消息角色：user / assistant / system / tool
     */
    private String role;

    /**
     * 消息文本内容
     */
    private String content;

    /**
     * assistant 发起的工具调用（JSON 数组，遵循 OpenAI 格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String toolCalls;

    /**
     * tool 角色消息对应的工具调用 ID
     */
    private String toolCallId;

    /**
     * 实际调用的模型名称（assistant 消息填写）
     */
    private String modelName;

    /**
     * 本次请求 prompt token 数
     */
    private Integer promptTokens;

    /**
     * 本次请求 completion token 数
     */
    private Integer completionTokens;

    /**
     * 本次请求总 token 数
     */
    private Integer totalTokens;

    /**
     * 结束原因：stop / tool_calls / length
     */
    private String finishReason;

    /**
     * 模型响应耗时（毫秒）
     */
    private Integer durationMs;
}
