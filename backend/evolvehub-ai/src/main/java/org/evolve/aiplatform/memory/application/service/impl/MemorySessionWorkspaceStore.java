package org.evolve.aiplatform.memory.application.service.impl;

import org.evolve.aiplatform.memory.domain.bean.dto.MemorySessionWorkspaceDTO;

import java.util.Optional;

/**
 * 会话工作区存储接口
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
interface MemorySessionWorkspaceStore {

    /**
     * 读取工作区
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @return 工作区
     * @author TellyJiang
     * @since 2026-04-16
     */
    Optional<MemorySessionWorkspaceDTO> get(Long userId, String sessionId);

    /**
     * 保存工作区
     *
     * @param workspace 工作区
     * @author TellyJiang
     * @since 2026-04-16
     */
    void save(MemorySessionWorkspaceDTO workspace);
}
