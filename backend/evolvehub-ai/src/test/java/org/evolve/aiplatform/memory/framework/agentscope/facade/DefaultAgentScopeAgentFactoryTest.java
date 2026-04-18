package org.evolve.aiplatform.memory.framework.agentscope.facade;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.LongTermMemory;
import io.agentscope.core.memory.StaticLongTermMemoryHook;
import io.agentscope.core.model.Model;
import org.evolve.aiplatform.memory.framework.agentscope.AgentScopeMemoryRuntimeService;
import org.evolve.aiplatform.memory.framework.agentscope.model.AgentScopeModelResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * AgentScope 工厂测试
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
class DefaultAgentScopeAgentFactoryTest {

    @Test
    void shouldCreateAgentWithConfiguredMemory() {
        DefaultAgentScopeAgentFactory factory = new DefaultAgentScopeAgentFactory();
        AgentScopeMemoryRuntimeService runtimeService = Mockito.mock(AgentScopeMemoryRuntimeService.class);
        Model model = Mockito.mock(Model.class);
        InMemoryMemory memory = Mockito.mock(InMemoryMemory.class);
        LongTermMemory longTermMemory = Mockito.mock(LongTermMemory.class);
        ReflectionTestUtils.setField(factory, "agentScopeMemoryRuntimeService", runtimeService);
        ReflectionTestUtils.setField(factory, "agentScopeModelResolver", (AgentScopeModelResolver) modelName -> model);
        Mockito.when(runtimeService.createShortTermMemory()).thenReturn(memory);
        Mockito.when(runtimeService.getLongTermMemory(1L, "session-a")).thenReturn(longTermMemory);
        Mockito.when(model.getModelName()).thenReturn("mock-model");

        ReActAgent agent = factory.create(1L, "session-a", "gpt");

        Assertions.assertNotNull(agent);
        Assertions.assertSame(memory, agent.getMemory());
        Object hooks = ReflectionTestUtils.getField(agent, "hooks");
        Assertions.assertNotNull(hooks);
        Assertions.assertTrue(((java.util.List<?>) hooks).stream().anyMatch(StaticLongTermMemoryHook.class::isInstance));
    }
}
