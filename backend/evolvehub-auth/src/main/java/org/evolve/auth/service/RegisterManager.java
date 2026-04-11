package org.evolve.auth.service;

import cn.hutool.crypto.digest.BCrypt;
import jakarta.annotation.Resource;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.UsersEntity;
import org.evolve.auth.request.RegisterRequest;
import org.evolve.auth.response.RegisterResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 注册业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>用户名全局唯一</li>
 *     <li>邮箱全局唯一（若传值）</li>
 *     <li>手机号全局唯一（若传值）</li>
 *     <li>密码 BCrypt 加密存储</li>
 *     <li>新用户默认状态正常（status=1），无部门归属</li>
 * </ul>
 *
 * @author zhao
 */
@Service
public class RegisterManager extends BaseManager<RegisterRequest, RegisterResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(RegisterRequest request) {
        if (usersInfra.getByUsername(request.username()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "用户名已存在");
        }
        if (request.email() != null && !request.email().isBlank()) {
            if (usersInfra.getByEmail(request.email()) != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "邮箱已被使用");
            }
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            if (usersInfra.getByPhone(request.phone()) != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "手机号已被使用");
            }
        }
    }

    @Override
    protected RegisterResponse process(RegisterRequest request) {
        UsersEntity entity = new UsersEntity();
        entity.setUsername(request.username());
        entity.setPassword(BCrypt.hashpw(request.password()));
        entity.setNickname(request.nickname());
        entity.setEmail(request.email());
        entity.setPhone(request.phone());
        entity.setStatus(1);
        usersInfra.createUser(entity);
        return new RegisterResponse(entity.getId(), entity.getUsername());
    }
}
