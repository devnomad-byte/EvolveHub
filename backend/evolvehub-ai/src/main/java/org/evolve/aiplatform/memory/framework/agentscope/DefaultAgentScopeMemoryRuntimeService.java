package org.evolve.aiplatform.memory.framework.agentscope;

import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.LongTermMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.SimpleSessionKey;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认 AgentScope Memory 运行时服务实现
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Service
public class DefaultAgentScopeMemoryRuntimeService implements AgentScopeMemoryRuntimeService {

    @Resource
    private Session agentScopeSession;

    @Resource
    private AgentScopeMsgMapper agentScopeMsgMapper;

    @Lazy
    @Resource(name = "memoryApiImpl")
    private MemoryApi memoryApi;

    @Override
    public Session getSession() {
        return agentScopeSession;
    }

    @Override
    public SessionKey buildSessionKey(Long userId, String sessionId) {
        return SimpleSessionKey.of("user-" + userId + ":session-" + sessionId);
    }

    @Override
    public InMemoryMemory createShortTermMemory() {
        return new InMemoryMemory();
    }

    @Override
    public Memory loadShortTermMemory(Long userId, String sessionId) {
        InMemoryMemory memory = createShortTermMemory();
        memory.loadIfExists(getSession(), buildSessionKey(userId, sessionId));
        return memory;
    }

    @Override
    public void saveShortTermMemory(Long userId, String sessionId, Memory memory) {
        memory.saveTo(getSession(), buildSessionKey(userId, sessionId));
    }

    @Override
    public LongTermMemory getLongTermMemory(Long userId, String sessionId) {
        return new MilvusLongTermMemoryAdapter(
                memoryApi,
                userId,
                sessionId
        );
    }

    @Override
    public Msg toMsg(Long userId, String sessionId, String role, String content, String modelName) {
        return agentScopeMsgMapper.toMsg(userId, sessionId, role, content, modelName);
    }

    @Override
    public List<Msg> toMsgs(Long userId, String sessionId, String role, List<String> messages, String modelName) {
        return agentScopeMsgMapper.toMsgs(userId, sessionId, role, messages, modelName);
    }
}
