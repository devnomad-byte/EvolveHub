package org.evolve.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.UserRolesInfra;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 删除用户业务处理器
 *
 * @author zhao
 */
@Service
public class UserDeleteManager extends BaseManager<Long, Void> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(Long userId) {
        // 检查用户是否存在
        if (usersInfra.getById(userId) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }

        // 不能删除自己
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(userId)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能删除自己");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected Void process(Long userId) {
        // 删除用户角色关联
        userRolesInfra.removeByUserId(userId);

        // 删除用户（软删除）
        usersInfra.deleteUser(userId);

        // 强制下线
        StpUtil.kickout(userId);

        return null;
    }
}
