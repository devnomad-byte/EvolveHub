package org.evolve.auth.service;

import cn.hutool.crypto.digest.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.auth.request.ResetPasswordRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 重置密码业务处理器（管理员重置用户密码）
 *
 * @author zhao
 */
@Service
public class ResetPasswordManager extends BaseManager<ResetPasswordRequest, Void> {

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(ResetPasswordRequest request) {
        // 检查用户是否存在
        UsersEntity user = usersInfra.getById(request.userId());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
    }

    @Override
    protected Void process(ResetPasswordRequest request) {
        // 查询用户
        UsersEntity user = usersInfra.getById(request.userId());

        // 重置密码
        user.setPassword(BCrypt.hashpw(request.newPassword()));
        usersInfra.updateUser(user);

        // 强制下线（让用户重新登录）
        StpUtil.kickout(user.getId());

        return null;
    }
}
