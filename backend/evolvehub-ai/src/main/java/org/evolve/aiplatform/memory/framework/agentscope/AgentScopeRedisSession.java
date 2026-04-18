package org.evolve.aiplatform.memory.framework.agentscope;

import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.State;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.session.Session;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 基于 Spring Redis 的 AgentScope Session 实现
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
public class AgentScopeRedisSession implements Session {

    private final StringRedisTemplate stringRedisTemplate;

    private final MemoryRuntimeJsonCodec memoryRuntimeJsonCodec;

    private final String keyPrefix;

    /**
     * 构造 Redis Session
     *
     * @param stringRedisTemplate Redis 模板
     * @param memoryRuntimeJsonCodec JSON 编解码器
     * @param keyPrefix 键前缀
     * @author TellyJiang
     * @since 2026-04-12
     */
    public AgentScopeRedisSession(StringRedisTemplate stringRedisTemplate,
                                  MemoryRuntimeJsonCodec memoryRuntimeJsonCodec,
                                  String keyPrefix) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.memoryRuntimeJsonCodec = memoryRuntimeJsonCodec;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void save(SessionKey sessionKey, String stateName, State state) {
        saveInternal(sessionKey, stateName, memoryRuntimeJsonCodec.toJson(state));
    }

    @Override
    public void save(SessionKey sessionKey, String stateName, List<? extends State> states) {
        saveInternal(sessionKey, stateName, memoryRuntimeJsonCodec.toJson(states));
    }

    @Override
    public <T extends State> Optional<T> get(SessionKey sessionKey, String stateName, Class<T> stateClass) {
        String payload = stringRedisTemplate.opsForValue().get(buildStateKey(sessionKey, stateName));
        if (payload == null || payload.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(memoryRuntimeJsonCodec.fromJson(payload, stateClass));
    }

    @Override
    public <T extends State> List<T> getList(SessionKey sessionKey, String stateName, Class<T> stateClass) {
        String payload = stringRedisTemplate.opsForValue().get(buildStateKey(sessionKey, stateName));
        if (payload == null || payload.isBlank()) {
            return List.of();
        }
        return memoryRuntimeJsonCodec.convertList(payload, stateClass);
    }

    @Override
    public boolean exists(SessionKey sessionKey) {
        Set<String> members = stringRedisTemplate.opsForSet().members(buildIndexKey(sessionKey));
        return members != null && !members.isEmpty();
    }

    @Override
    public void delete(SessionKey sessionKey) {
        String indexKey = buildIndexKey(sessionKey);
        Set<String> keys = stringRedisTemplate.opsForSet().members(indexKey);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        stringRedisTemplate.delete(indexKey);
    }

    @Override
    public Set<SessionKey> listSessionKeys() {
        Set<String> keys = stringRedisTemplate.keys(keyPrefix + ":index:*");
        if (keys == null || keys.isEmpty()) {
            return Set.of();
        }
        Set<SessionKey> sessionKeys = new LinkedHashSet<>();
        String prefix = keyPrefix + ":index:";
        for (String key : keys) {
            if (!key.startsWith(prefix) || key.length() <= prefix.length()) {
                continue;
            }
            sessionKeys.add(SimpleSessionKey.of(key.substring(prefix.length())));
        }
        return sessionKeys;
    }

    private void saveInternal(SessionKey sessionKey, String stateName, String payload) {
        String stateKey = buildStateKey(sessionKey, stateName);
        stringRedisTemplate.opsForValue().set(stateKey, payload, MemoryConstants.MEMORY_SESSION_TTL);
        String indexKey = buildIndexKey(sessionKey);
        Long added = stringRedisTemplate.opsForSet().add(indexKey, stateKey);
        if (added == null) {
            throw new BusinessException(ResultCode.FAIL, "保存 AgentScope Session 失败: " + sessionKey.toIdentifier());
        }
        stringRedisTemplate.expire(indexKey, MemoryConstants.MEMORY_SESSION_TTL);
    }

    private String buildStateKey(SessionKey sessionKey, String stateName) {
        return keyPrefix + ":state:" + sessionKey.toIdentifier() + ":" + stateName;
    }

    private String buildIndexKey(SessionKey sessionKey) {
        return keyPrefix + ":index:" + sessionKey.toIdentifier();
    }
}
