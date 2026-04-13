package org.evolve.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.annotation.Resource;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.auth.request.LoginRequest;
import org.evolve.auth.response.LoginResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 登录业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>用户名必须存在且状态正常</li>
 *     <li>密码 BCrypt 校验</li>
 *     <li>登录成功后调用 StpUtil.login() 生成 token</li>
 *     <li>返回 token + 用户基本信息 + 角色/权限列表</li>
 * </ul>
 *
 * @author zhao
 */
@Service
public class LoginManager extends BaseManager<LoginRequest, LoginResponse> {

    @Resource
    private UsersInfra usersInfra;

    /** 暂存 check 阶段查出的用户实体，供 process 使用 */
    private final ThreadLocal<UsersEntity> userHolder = new ThreadLocal<>();

    @Override
    protected void check(LoginRequest request) {
        UsersEntity user = usersInfra.getByUsername(request.username());
        if (user == null) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED, "账号已被禁用");
        }
        if (!BCrypt.checkpw(request.password(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "用户名或密码错误");
        }
        userHolder.set(user);
    }

    @Override
    protected LoginResponse process(LoginRequest request) {
        try {
            UsersEntity user = userHolder.get();

            // Sa-Token 登录
            StpUtil.login(user.getId());

            return new LoginResponse(
                    StpUtil.getTokenValue(),
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    StpUtil.getRoleList(),
                    StpUtil.getPermissionList()
            );
        } finally {
            userHolder.remove();
        }
    }
}
