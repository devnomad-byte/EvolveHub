package org.evolve.admin.service;

import cn.hutool.crypto.digest.BCrypt;
import jakarta.annotation.Resource;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.model.UsersEntity;
import org.evolve.admin.request.CreateUserRequest;
import org.evolve.admin.response.CreateUserResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建用户业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>用户名全局唯一</li>
 *     <li>邮箱全局唯一（若传值）</li>
 *     <li>手机号全局唯一（若传值）</li>
 *     <li>所属部门必须存在且状态正常</li>
 *     <li>密码使用 BCrypt 加密存储</li>
 *     <li>新用户默认状态为正常（status=1）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class CreateUserManager extends BaseManager<CreateUserRequest, CreateUserResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(CreateUserRequest request) {
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
        DeptEntity dept = deptInfra.getDeptById(request.deptId());
        if (dept == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "所属部门不存在");
        }
        if (dept.getStatus() != null && dept.getStatus() == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "所属部门已被禁用");
        }
    }

    @Override
    protected CreateUserResponse process(CreateUserRequest request) {
        UsersEntity entity = new UsersEntity();
        entity.setUsername(request.username());
        entity.setPassword(BCrypt.hashpw(request.password()));
        entity.setNickname(request.nickname());
        entity.setEmail(request.email());
        entity.setPhone(request.phone());
        entity.setDeptId(request.deptId());
        entity.setStatus(1);
        usersInfra.createUser(entity);
        return new CreateUserResponse(entity.getId());
    }
}
