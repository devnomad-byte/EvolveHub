package org.evolve.aiplatform.memory.framework.agentscope.facade;

import io.agentscope.core.ReActAgent;

/**
 * AgentScope 智能体工厂接口
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface AgentScopeAgentFactory {

    /**
     * 创建 AgentScope 智能体
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param modelName 模型名称
     * @return 智能体
     * @author TellyJiang
     * @since 2026-04-14
     */
    ReActAgent create(Long userId, String sessionId, String modelName);
}
