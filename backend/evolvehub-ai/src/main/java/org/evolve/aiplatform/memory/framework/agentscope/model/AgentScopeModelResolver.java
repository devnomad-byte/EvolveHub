package org.evolve.aiplatform.memory.framework.agentscope.model;

import io.agentscope.core.model.Model;

/**
 * AgentScope 模型解析接口
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface AgentScopeModelResolver {

    /**
     * 解析模型实例
     *
     * @param modelName 模型名称
     * @return 模型实例
     * @author TellyJiang
     * @since 2026-04-14
     */
    Model resolve(String modelName);
}
