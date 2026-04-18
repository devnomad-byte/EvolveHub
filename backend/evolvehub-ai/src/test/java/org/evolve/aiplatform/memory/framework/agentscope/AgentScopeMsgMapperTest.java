package org.evolve.aiplatform.memory.framework.agentscope;

import io.agentscope.core.message.MsgRole;
import org.evolve.aiplatform.memory.framework.agentscope.AgentScopeMsgMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * AgentScope 消息映射测试
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
class AgentScopeMsgMapperTest {

    @Test
    void shouldMapUserMessageIntoAgentScopeMsg() {
        AgentScopeMsgMapper mapper = new AgentScopeMsgMapper();
        var msg = mapper.toMsg(1L, "session-a", "user", "你好", "gpt-4.1");
        Assertions.assertEquals(MsgRole.USER, msg.getRole());
        Assertions.assertEquals("你好", msg.getTextContent());
        Assertions.assertEquals("session-a", msg.getMetadata().get("sessionId"));
    }

    @Test
    void shouldDefaultRoleToUserWhenBlank() {
        AgentScopeMsgMapper mapper = new AgentScopeMsgMapper();
        var msg = mapper.toMsg(1L, "session-a", "", "hello", null);
        Assertions.assertEquals(MsgRole.USER, msg.getRole());
    }
}
