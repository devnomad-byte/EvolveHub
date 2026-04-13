package org.evolve.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.annotation.Resource;
import org.evolve.auth.request.PasswordChangeRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.UsersEntity;
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

    /** 暂存 check 阶段查出的用户实体，供 process 使用 */
    private final ThreadLocal<UsersEntity> userHolder = new ThreadLocal<>();

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

        // 暂存用户实体，避免重复查询
        userHolder.set(user);
    }

    @Override
    protected Void process(PasswordChangeRequest request) {
        try {
            UsersEntity user = userHolder.get();

            // 更新密码
            user.setPassword(BCrypt.hashpw(request.newPassword()));
            usersInfra.updateUser(user);

            // 注意：用户自己修改密码不应强制下线，保持登录状态
            // 如果需要增强安全性，可以考虑让用户重新输入密码验证，但不要踢出登录

            return null;
        } finally {
            userHolder.remove();
        }
    }
}
