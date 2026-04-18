package org.evolve.aiplatform.memory.framework.agentscope.facade;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.framework.agentscope.AgentScopeMemoryRuntimeService;
import org.springframework.stereotype.Component;

/**
 * 默认 AgentScope 对话门面
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultAgentScopeConversationFacade implements AgentScopeConversationFacade {

    @Resource
    private AgentScopeAgentFactory agentScopeAgentFactory;

    @Resource
    private AgentScopeMemoryRuntimeService agentScopeMemoryRuntimeService;

    @Override
    public Msg chat(Long userId, String sessionId, String modelName, String userMessage) {
        ReActAgent agent = agentScopeAgentFactory.create(userId, sessionId, modelName);
        agent.loadIfExists(
                agentScopeMemoryRuntimeService.getSession(),
                agentScopeMemoryRuntimeService.buildSessionKey(userId, sessionId)
        );
        Msg response = agent.call(agentScopeMemoryRuntimeService.toMsg(
                userId,
                sessionId,
                "user",
                userMessage,
                modelName
        )).block();
        agent.saveTo(
                agentScopeMemoryRuntimeService.getSession(),
                agentScopeMemoryRuntimeService.buildSessionKey(userId, sessionId)
        );
        return response;
    }
}
