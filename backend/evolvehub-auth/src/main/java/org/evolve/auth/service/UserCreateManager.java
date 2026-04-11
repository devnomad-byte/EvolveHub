package org.evolve.auth.service;

import cn.hutool.crypto.digest.BCrypt;
import jakarta.annotation.Resource;
import org.evolve.auth.request.UserCreateRequest;
import org.evolve.auth.response.UserResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.infra.RolesInfra;
import org.evolve.common.infra.UserRolesInfra;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.model.RolesEntity;
import org.evolve.common.model.UserRolesEntity;
import org.evolve.common.model.UsersEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 创建用户业务处理器
 *
 * @author zhao
 */
@Service
public class UserCreateManager extends BaseManager<UserCreateRequest, UserResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(UserCreateRequest request) {
        // 检查用户名唯一性
        if (usersInfra.getByUsername(request.username()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "用户名已存在");
        }

        // 检查邮箱唯一性（如果提供）
        if (request.email() != null && !request.email().isBlank()) {
            if (usersInfra.getByEmail(request.email()) != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "邮箱已被使用");
            }
        }

        // 检查手机号唯一性（如果提供）
        if (request.phone() != null && !request.phone().isBlank()) {
            if (usersInfra.getByPhone(request.phone()) != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "手机号已被使用");
            }
        }

        // 检查部门是否存在
        DeptEntity dept = deptInfra.getById(request.deptId());
        if (dept == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
        }

        // 检查角色是否存在
        RolesEntity role = rolesInfra.getRoleById(request.roleId());
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected UserResponse process(UserCreateRequest request) {
        // 创建用户
        UsersEntity user = new UsersEntity();
        user.setUsername(request.username());
        user.setPassword(BCrypt.hashpw(request.password()));
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setDeptId(request.deptId());
        user.setStatus(request.status() != null ? request.status() : 1);
        usersInfra.createUser(user);

        // 分配角色
        UserRolesEntity userRole = new UserRolesEntity();
        userRole.setUserId(user.getId());
        userRole.setRoleId(request.roleId());
        userRolesInfra.assignRole(userRole);

        // 查询完整信息返回
        return buildUserResponse(user);
    }

    private UserResponse buildUserResponse(UsersEntity user) {
        // 查询部门信息
        DeptEntity dept = deptInfra.getById(user.getDeptId());
        String deptName = dept != null ? dept.getDeptName() : null;

        // 查询角色信息
        UserRolesEntity userRole = userRolesInfra.listByUserId(user.getId()).stream().findFirst().orElse(null);
        RolesEntity role = null;
        if (userRole != null) {
            role = rolesInfra.getRoleById(userRole.getRoleId());
        }

        // 构建角色信息
        UserResponse.RoleInfo roleInfo = null;
        if (role != null) {
            roleInfo = new UserResponse.RoleInfo(role.getId(), role.getRoleName(), role.getRoleCode());
        }

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getDeptId(),
                deptName,
                roleInfo != null ? List.of(roleInfo) : List.of(),
                user.getStatus(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }
}
