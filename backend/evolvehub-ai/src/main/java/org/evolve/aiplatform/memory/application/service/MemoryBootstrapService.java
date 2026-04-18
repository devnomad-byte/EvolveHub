package org.evolve.aiplatform.memory.application.service;

/**
 * 用户记忆初始化服务
 *
 * @author TellyJiang
 * @since 2026-04-18
 */
public interface MemoryBootstrapService {

    /**
     * 为管理员新建用户初始化默认画像
     *
     * @param userId 用户 ID
     * @param deptId 部门 ID
     * @author TellyJiang
     * @since 2026-04-18
     */
    void initializeForAdminCreatedUser(Long userId, Long deptId);
}
