package org.evolve.aiplatform.memory.framework.agentscope;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AgentScope 消息映射器
 * 
 * 负责业务侧字符串消息与 AgentScope Msg 之间的双向映射。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Component
public class AgentScopeMsgMapper {

    /**
     * 生成 AgentScope Msg
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param role 角色
     * @param content 文本内容
     * @param modelName 模型名称
     * @return Msg 对象
     * @author TellyJiang
     * @since 2026-04-12
     */
    public Msg toMsg(Long userId, String sessionId, String role, String content, String modelName) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", userId);
        metadata.put("sessionId", sessionId);
        metadata.put("modelName", modelName);
        return Msg.builder()
                .name("memory-runtime")
                .role(resolveRole(role))
                .textContent(content == null ? "" : content)
                .metadata(metadata)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC).toString())
                .build();
    }

    /**
     * 批量构造 AgentScope Msg
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param role 角色
     * @param messages 文本消息
     * @param modelName 模型名称
     * @return Msg 列表
     * @author TellyJiang
     * @since 2026-04-12
     */
    public List<Msg> toMsgs(Long userId, String sessionId, String role, List<String> messages, String modelName) {
        if (messages == null) {
            return List.of();
        }
        return messages.stream()
                .map(message -> toMsg(userId, sessionId, role, message, modelName))
                .toList();
    }

    /**
     * 提取消息文本
     *
     * @param msg AgentScope 消息
     * @return 文本内容
     * @author TellyJiang
     * @since 2026-04-12
     */
    public String getTextContent(Msg msg) {
        return msg == null ? "" : msg.getTextContent();
    }

    private MsgRole resolveRole(String role) {
        if (role == null || role.isBlank()) {
            return MsgRole.USER;
        }
        return switch (role.trim().toLowerCase()) {
            case "assistant" -> MsgRole.ASSISTANT;
            case "system" -> MsgRole.SYSTEM;
            case "tool" -> MsgRole.TOOL;
            default -> MsgRole.USER;
        };
    }
}
