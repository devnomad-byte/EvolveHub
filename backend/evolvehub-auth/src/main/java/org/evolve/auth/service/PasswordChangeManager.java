package org.evolve.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.annotation.Resource;
import org.evolve.auth.request.PasswordChangeRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.UsersEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 修改密码业务处理器
 *
 * @author zhao
 */
@Service
public class PasswordChangeManager extends BaseManager<PasswordChangeRequest, Void> {

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(PasswordChangeRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        UsersEntity user = usersInfra.getById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }

        // 验证旧密码
        if (!BCrypt.checkpw(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }
    }

    @Override
    protected Void process(PasswordChangeRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        UsersEntity user = usersInfra.getById(userId);

        // 更新密码
        user.setPassword(BCrypt.hashpw(request.newPassword()));
        usersInfra.updateUser(user);

        // 重新登录（强制下线所有设备）
        StpUtil.kickout(userId);

        return null;
    }
}
