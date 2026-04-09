package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.DeptInfra;
import org.evolve.auth.user.infra.UsersInfra;
import org.evolve.auth.user.model.UsersEntity;
import org.evolve.auth.user.request.UpdateUserRequest;
import org.evolve.auth.user.response.UpdateUserResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新用户业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>用户必须存在</li>
 *     <li>邮箱若修改，需保证全局唯一（排除自身）</li>
 *     <li>手机号若修改，需保证全局唯一（排除自身）</li>
 *     <li>部门若修改，目标部门必须存在</li>
 *     <li>不允许修改用户名（登录账号不可变更）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class UpdateUserManager extends BaseManager<UpdateUserRequest, UpdateUserResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(UpdateUserRequest request) {
        // 用户必须存在
        if (usersInfra.getUserById(request.id()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
        // 邮箱排他唯一性校验
        if (request.email() != null && !request.email().isBlank()) {
            UsersEntity existUser = usersInfra.getByEmail(request.email());
            if (existUser != null && !existUser.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "邮箱已被其他用户使用");
            }
        }
        // 手机号排他唯一性校验
        if (request.phone() != null && !request.phone().isBlank()) {
            UsersEntity existUser = usersInfra.getByPhone(request.phone());
            if (existUser != null && !existUser.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "手机号已被其他用户使用");
            }
        }
        // 部门存在性校验
        if (request.deptId() != null && deptInfra.getDeptById(request.deptId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "目标部门不存在");
        }
    }

    @Override
    protected UpdateUserResponse process(UpdateUserRequest request) {
        UsersEntity entity = new UsersEntity();
        entity.setId(request.id());
        entity.setNickname(request.nickname());
        entity.setEmail(request.email());
        entity.setPhone(request.phone());
        entity.setAvatar(request.avatar());
        entity.setDeptId(request.deptId());
        entity.setStatus(request.status());
        usersInfra.updateUser(entity);
        return new UpdateUserResponse();
    }
}
