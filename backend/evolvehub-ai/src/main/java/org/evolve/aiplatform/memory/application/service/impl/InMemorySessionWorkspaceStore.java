package org.evolve.aiplatform.memory.application.service.impl;

import org.evolve.aiplatform.memory.domain.bean.dto.MemorySessionWorkspaceDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 JVM 的会话工作区存储
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
@Component
class InMemorySessionWorkspaceStore implements MemorySessionWorkspaceStore {

    private final Map<String, MemorySessionWorkspaceDTO> workspaceCache = new ConcurrentHashMap<>();

    @Override
    public Optional<MemorySessionWorkspaceDTO> get(Long userId, String sessionId) {
        return Optional.ofNullable(workspaceCache.get(buildKey(userId, sessionId)));
    }

    @Override
    public void save(MemorySessionWorkspaceDTO workspace) {
        if (workspace == null || workspace.getUserId() == null || workspace.getSessionId() == null) {
            return;
        }
        workspaceCache.put(buildKey(workspace.getUserId(), workspace.getSessionId()), workspace);
    }

    private String buildKey(Long userId, String sessionId) {
        return userId + ":" + sessionId;
    }
}
