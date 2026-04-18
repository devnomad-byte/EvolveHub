package org.evolve.aiplatform.memory.application.service.impl;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Component;

/**
 * Memory 访问控制守卫
 * 
 * 负责在进入 memory 核心功能前校验当前操作者是否具备访问目标用户记忆的权限。
 * 该类是 memory 子系统的统一前置校验组件，不参与具体业务处理。
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Component
class MemoryAccessGuard {

    private final MemoryOperatorContext memoryOperatorContext;

    /**
     * 构造方法
     *
     * @param memoryOperatorContext 操作者上下文
     * @author TellyJiang
     * @since 2026-04-11
     */
    MemoryAccessGuard(MemoryOperatorContext memoryOperatorContext) {
        this.memoryOperatorContext = memoryOperatorContext;
    }

    /**
     * 校验当前操作者是否可访问目标用户记忆
     *
     * @param targetUserId 目标用户 ID
     * @author TellyJiang
     * @since 2026-04-11
     */
    public void assertCanAccessUser(Long targetUserId) {
        if (targetUserId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "目标用户ID不能为空");
        }
        Long currentUserId = memoryOperatorContext.getCurrentUserId();
        if (!currentUserId.equals(targetUserId) && !memoryOperatorContext.isAdminOperator()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "禁止访问其他用户的记忆数据");
        }
    }

    /**
     * 校验画像更新请求权限
     *
     * @param profile 画像对象
     * @author TellyJiang
     * @since 2026-04-11
     */
    public void assertCanUpdateProfile(MemoryProfileDTO profile) {
        if (profile == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "画像对象不能为空");
        }
        assertCanAccessUser(profile.getUserId());
    }

    /**
     * 校验结构化记忆写入权限
     *
     * @param item 结构化记忆
     * @author TellyJiang
     * @since 2026-04-11
     */
    public void assertCanWriteStructuredItem(MemoryStructuredItemDTO item) {
        if (item == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结构化记忆不能为空");
        }
        assertCanAccessUser(item.getUserId());
    }

    /**
     * 校验记忆提取请求权限
     *
     * @param request 提取请求
     * @author TellyJiang
     * @since 2026-04-11
     */
    public void assertCanExtract(MemoryExtractionRequestDTO request) {
        if (request == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "提取请求不能为空");
        }
        assertCanAccessUser(request.getUserId());
    }
}
