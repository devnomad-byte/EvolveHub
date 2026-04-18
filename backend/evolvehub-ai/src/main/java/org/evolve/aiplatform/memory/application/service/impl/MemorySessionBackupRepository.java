package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.domain.bean.dto.MemorySessionWorkspaceDTO;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.framework.agentscope.MemoryRuntimeJsonCodec;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 会话工作区 Redis 备份仓储
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
@Repository
class MemorySessionBackupRepository {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MemoryRuntimeJsonCodec memoryRuntimeJsonCodec;

    /**
     * 读取 Redis 备份
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @return 工作区
     * @author TellyJiang
     * @since 2026-04-16
     */
    public Optional<MemorySessionWorkspaceDTO> load(Long userId, String sessionId) {
        String payload = stringRedisTemplate.opsForValue().get(buildKey(userId, sessionId));
        if (payload == null || payload.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(memoryRuntimeJsonCodec.fromJson(payload, MemorySessionWorkspaceDTO.class));
    }

    /**
     * 保存 Redis 备份
     *
     * @param workspace 工作区
     * @author TellyJiang
     * @since 2026-04-16
     */
    public void save(MemorySessionWorkspaceDTO workspace) {
        if (workspace == null || workspace.getUserId() == null || workspace.getSessionId() == null) {
            return;
        }
        stringRedisTemplate.opsForValue().set(
                buildKey(workspace.getUserId(), workspace.getSessionId()),
                memoryRuntimeJsonCodec.toJson(workspace),
                MemoryConstants.MEMORY_SESSION_TTL
        );
    }

    private String buildKey(Long userId, String sessionId) {
        return String.format(MemoryConstants.MEMORY_SESSION_KEY_TEMPLATE, userId, sessionId);
    }
}
