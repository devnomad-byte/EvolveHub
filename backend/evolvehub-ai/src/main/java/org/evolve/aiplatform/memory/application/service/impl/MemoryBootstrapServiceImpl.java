package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.application.service.MemoryBootstrapService;
import org.evolve.aiplatform.memory.application.service.MemoryProfileService;
import org.evolve.aiplatform.utils.S3Util;
import org.springframework.stereotype.Service;

/**
 * 用户记忆初始化服务实现
 *
 * @author TellyJiang
 * @since 2026-04-18
 */
@Service
class MemoryBootstrapServiceImpl implements MemoryBootstrapService {

    @Resource(name = "memoryProfileServiceImpl")
    private MemoryProfileService memoryProfileService;

    @Resource(name = "aiS3Util")
    private S3Util s3Util;

    /**
     * 初始化管理员创建用户的默认画像
     *
     * @param userId 用户 ID
     * @param deptId 部门 ID
     * @author TellyJiang
     * @since 2026-04-18
     */
    @Override
    public void initializeForAdminCreatedUser(Long userId, Long deptId) {
        s3Util.ensureBucketExists();
        memoryProfileService.initializeMemoryProfile(userId);
    }
}
