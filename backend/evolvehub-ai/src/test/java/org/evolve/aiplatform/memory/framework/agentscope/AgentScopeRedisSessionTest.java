package org.evolve.aiplatform.memory.framework.agentscope;

import io.agentscope.core.state.SimpleSessionKey;
import org.evolve.aiplatform.memory.framework.agentscope.AgentScopeMsgMapper;
import org.evolve.aiplatform.memory.framework.agentscope.AgentScopeRedisSession;
import org.evolve.aiplatform.memory.framework.agentscope.MemoryRuntimeJsonCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * AgentScope Redis Session 测试
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
class AgentScopeRedisSessionTest {

    @Test
    void shouldSaveAndLoadMessageState() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", 6379);
        try {
            factory.afterPropertiesSet();
            factory.getConnection().ping();
        } catch (Exception exception) {
            Assumptions.abort("本地 Redis 未就绪，跳过 AgentScope Redis Session 测试");
            return;
        }
        StringRedisTemplate template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        AgentScopeRedisSession session = new AgentScopeRedisSession(
                template,
                new MemoryRuntimeJsonCodec(new com.fasterxml.jackson.databind.ObjectMapper()),
                "test:agentscope"
        );
        AgentScopeMsgMapper mapper = new AgentScopeMsgMapper();
        var sessionKey = SimpleSessionKey.of("user-1:session-1");
        var msg = mapper.toMsg(1L, "1", "user", "hello", null);
        session.save(sessionKey, "memory", msg);
        var restored = session.get(sessionKey, "memory", msg.getClass());
        Assertions.assertTrue(restored.isPresent());
        Assertions.assertEquals("hello", restored.get().getTextContent());
        Assertions.assertTrue(session.listSessionKeys().stream()
                .anyMatch(key -> "user-1:session-1".equals(key.toIdentifier())));
        session.delete(sessionKey);
        factory.destroy();
    }
}
