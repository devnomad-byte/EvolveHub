package org.evolve.aiplatform.memory.framework.agentscope.facade;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.LongTermMemoryMode;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.model.Model;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.framework.agentscope.AgentScopeMemoryRuntimeService;
import org.evolve.aiplatform.memory.framework.agentscope.model.AgentScopeModelResolver;
import org.springframework.stereotype.Component;

/**
 * 默认 AgentScope 智能体工厂
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultAgentScopeAgentFactory implements AgentScopeAgentFactory {

    @Resource
    private AgentScopeMemoryRuntimeService agentScopeMemoryRuntimeService;

    @Resource
    private AgentScopeModelResolver agentScopeModelResolver;

    @Override
    public ReActAgent create(Long userId, String sessionId, String modelName) {
        Model model = agentScopeModelResolver.resolve(modelName);
        Memory memory = agentScopeMemoryRuntimeService.createShortTermMemory();
        return ReActAgent.builder()
                .name("EvolveHubAssistant")
                .description("EvolveHub AgentScope 对话智能体")
                .model(model)
                .memory(memory)
                .longTermMemory(agentScopeMemoryRuntimeService.getLongTermMemory(userId, sessionId))
                .longTermMemoryMode(LongTermMemoryMode.STATIC_CONTROL)
                .build();
    }
}
