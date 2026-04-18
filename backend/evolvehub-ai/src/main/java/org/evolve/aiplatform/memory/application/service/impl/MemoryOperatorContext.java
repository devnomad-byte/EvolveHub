package org.evolve.aiplatform.memory.application.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.common.base.CurrentUserHolder;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Component;

/**
 * Memory 操作者上下文
 * 
 * 负责从通用用户上下文中读取当前操作者身份，为 memory 模块的权限判断提供统一入口。
 * 该类仅承载上下文读取职责，不包含业务规则。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Component
class MemoryOperatorContext {

    @Resource
    private UsersInfra usersInfra;

    /**
     * 获取当前操作者用户 ID
     *
     * @return 当前用户 ID
     * @author TellyJiang
     * @since 2026-04-11
     */
    public Long getCurrentUserId() {
        Long currentUserId = CurrentUserHolder.getUserId();
        if (currentUserId == null) {
            try {
                currentUserId = StpUtil.getLoginIdAsLong();
            } catch (Exception exception) {
                currentUserId = null;
            }
        }
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "当前未登录，禁止访问记忆数据");
        }
        return currentUserId;
    }

    /**
     * 获取当前操作者部门 ID
     *
     * @return 部门 ID
     * @author TellyJiang
     * @since 2026-04-16
     */
    public Long getCurrentDeptId() {
        UsersEntity user = usersInfra.getUserById(getCurrentUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "当前登录用户不存在，禁止访问记忆数据");
        }
        return user.getDeptId();
    }

    /**
     * 判断当前操作者是否为可跨用户管理记忆的管理员角色
     *
     * @return 是否管理员
     * @author TellyJiang
     * @since 2026-04-17
     */
    public boolean isAdminOperator() {
        try {
            return StpUtil.getRoleList().stream()
                    .anyMatch(roleCode -> "SUPER_ADMIN".equals(roleCode) || "ADMIN".equals(roleCode));
        } catch (Exception exception) {
            return false;
        }
    }
}
