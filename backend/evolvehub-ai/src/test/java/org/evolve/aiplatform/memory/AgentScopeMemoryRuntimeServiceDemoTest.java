package org.evolve.aiplatform.memory;

import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.LongTermMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.session.Session;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.aiplatform.memory.framework.agentscope.AgentScopeMsgMapper;
import org.evolve.aiplatform.memory.framework.agentscope.DefaultAgentScopeMemoryRuntimeService;
import org.evolve.aiplatform.memory.framework.agentscope.MilvusLongTermMemoryAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

/**
 * AgentScope Memory 运行时对接演示测试
 *
 * 该测试用于演示项目如何通过 AgentScope 抽象完成消息转换、短期记忆保存与长期记忆实例暴露。
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
class AgentScopeMemoryRuntimeServiceDemoTest {

    /**
     * 演示短期记忆如何通过 AgentScope Session 进行读写
     *
     * @author TellyJiang
     * @since 2026-04-14
     */
    @Test
    void shouldBridgeProjectMessageAndShortTermMemoryThroughAgentScope() {
        DefaultAgentScopeMemoryRuntimeService runtimeService = buildRuntimeService();
        Session session = Mockito.mock(Session.class);
        ReflectionTestUtils.setField(runtimeService, "agentScopeSession", session);

        Msg userMessage = runtimeService.toMsg(1001L, "demo-session", "user", "你好，AgentScope", "gpt-4.1");
        Memory shortTermMemory = Mockito.mock(Memory.class);

        runtimeService.saveShortTermMemory(1001L, "demo-session", shortTermMemory);

        Mockito.verify(shortTermMemory).saveTo(
                Mockito.same(session),
                Mockito.eq(runtimeService.buildSessionKey(1001L, "demo-session"))
        );

        InMemoryMemory restored = Mockito.spy(new InMemoryMemory());
        List<Msg> restoredMessages = List.of(
                userMessage,
                runtimeService.toMsg(1001L, "demo-session", "assistant", "你好，我已经记住上下文", "gpt-4.1")
        );
        Mockito.doReturn(restoredMessages).when(restored).getMessages();
        Mockito.doAnswer(invocation -> null).when(restored).loadIfExists(
                Mockito.same(session),
                Mockito.eq(runtimeService.buildSessionKey(1001L, "demo-session"))
        );
        DefaultAgentScopeMemoryRuntimeService loadRuntimeService = new DefaultAgentScopeMemoryRuntimeService() {
            @Override
            public Memory loadShortTermMemory(Long userId, String sessionId) {
                restored.loadIfExists(getSession(), buildSessionKey(userId, sessionId));
                return restored;
            }
        };
        ReflectionTestUtils.setField(loadRuntimeService, "agentScopeMsgMapper", new AgentScopeMsgMapper());
        ReflectionTestUtils.setField(loadRuntimeService, "memoryApi", Mockito.mock(MemoryApi.class));
        ReflectionTestUtils.setField(loadRuntimeService, "agentScopeSession", session);

        InMemoryMemory loadedMemory = (InMemoryMemory) loadRuntimeService.loadShortTermMemory(1001L, "demo-session");

        Mockito.verify(restored).loadIfExists(
                Mockito.same(session),
                Mockito.eq(runtimeService.buildSessionKey(1001L, "demo-session"))
        );
        Assertions.assertSame(restored, loadedMemory);
        Assertions.assertEquals(2, loadedMemory.getMessages().size());
        Assertions.assertEquals("你好，AgentScope", loadedMemory.getMessages().get(0).getTextContent());
        Assertions.assertEquals("你好，我已经记住上下文", loadedMemory.getMessages().get(1).getTextContent());
        Assertions.assertEquals("demo-session", loadedMemory.getMessages().get(0).getMetadata().get("sessionId"));
        Assertions.assertEquals("gpt-4.1", loadedMemory.getMessages().get(0).getMetadata().get("modelName"));
    }

    /**
     * 演示长期记忆如何以 AgentScope LongTermMemory 形式暴露给对话系统
     *
     * @author TellyJiang
     * @since 2026-04-14
     */
    @Test
    void shouldExposeProjectLongTermMemoryAsAgentScopeInterface() {
        DefaultAgentScopeMemoryRuntimeService runtimeService = buildRuntimeService();

        LongTermMemory longTermMemory = runtimeService.getLongTermMemory(1001L, "demo-session");

        Assertions.assertInstanceOf(MilvusLongTermMemoryAdapter.class, longTermMemory);
    }

    /**
     * 构造演示运行时服务
     *
     * @return 运行时服务
     * @author TellyJiang
     * @since 2026-04-14
     */
    private DefaultAgentScopeMemoryRuntimeService buildRuntimeService() {
        DefaultAgentScopeMemoryRuntimeService runtimeService = new DefaultAgentScopeMemoryRuntimeService();
        ReflectionTestUtils.setField(runtimeService, "agentScopeMsgMapper", new AgentScopeMsgMapper());
        ReflectionTestUtils.setField(runtimeService, "memoryApi", Mockito.mock(MemoryApi.class));
        return runtimeService;
    }
}
