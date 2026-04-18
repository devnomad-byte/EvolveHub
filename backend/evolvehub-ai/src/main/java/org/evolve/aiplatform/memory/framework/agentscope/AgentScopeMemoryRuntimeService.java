package org.evolve.aiplatform.memory.framework.agentscope;

import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.LongTermMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;

import java.util.List;

/**
 * AgentScope Memory 运行时服务
 * 
 * 该接口直接面向 AgentScope 对话系统暴露短期记忆、长期记忆与会话状态能力，
 * 是 memory 模块供外部 AgentScope 运行时接入的主入口。
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
public interface AgentScopeMemoryRuntimeService {

    /**
     * 获取 AgentScope Session 实例
     *
     * @return Session 对象
     * @author TellyJiang
     * @since 2026-04-12
     */
    Session getSession();

    /**
     * 构建 AgentScope SessionKey
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @return SessionKey
     * @author TellyJiang
     * @since 2026-04-12
     */
    SessionKey buildSessionKey(Long userId, String sessionId);

    /**
     * 创建短期记忆实例
     *
     * @return 短期记忆
     * @author TellyJiang
     * @since 2026-04-14
     */
    InMemoryMemory createShortTermMemory();

    /**
     * 加载短期记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @return Memory 对象
     * @author TellyJiang
     * @since 2026-04-12
     */
    Memory loadShortTermMemory(Long userId, String sessionId);

    /**
     * 保存短期记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param memory 短期记忆
     * @author TellyJiang
     * @since 2026-04-12
     */
    void saveShortTermMemory(Long userId, String sessionId, Memory memory);

    /**
     * 获取长期记忆能力
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @return 长期记忆实现
     * @author TellyJiang
     * @since 2026-04-12
     */
    LongTermMemory getLongTermMemory(Long userId, String sessionId);

    /**
     * 将业务文本转换为 AgentScope Msg
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param role 角色
     * @param content 内容
     * @param modelName 模型名称
     * @return Msg 对象
     * @author TellyJiang
     * @since 2026-04-12
     */
    Msg toMsg(Long userId, String sessionId, String role, String content, String modelName);

    /**
     * 批量将业务文本转换为 AgentScope Msg
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
    List<Msg> toMsgs(Long userId, String sessionId, String role, List<String> messages, String modelName);
}
