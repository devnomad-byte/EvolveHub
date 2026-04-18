package org.evolve.aiplatform.memory.framework.agentscope.facade;

import io.agentscope.core.message.Msg;

/**
 * AgentScope 对话门面接口
 *
 * 负责封装标准的 AgentScope 会话恢复、调用和保存流程。
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface AgentScopeConversationFacade {

    /**
     * 执行一次对话
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param modelName 模型名称
     * @param userMessage 用户消息
     * @return 智能体回复消息
     * @author TellyJiang
     * @since 2026-04-14
     */
    Msg chat(Long userId, String sessionId, String modelName, String userMessage);
}
