package org.evolve.admin.service;

import cn.hutool.crypto.digest.BCrypt;
import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateUserRequest;
import org.evolve.admin.response.UserResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.infra.RolesInfra;
import org.evolve.domain.rbac.infra.UserRolesInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.DeptEntity;
import org.evolve.domain.rbac.model.RolesEntity;
import org.evolve.domain.rbac.model.UserRolesEntity;
import org.evolve.domain.rbac.model.UsersEntity;
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
public class CreateUserManager extends BaseManager<CreateUserRequest, UserResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(CreateUserRequest request) {
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
        DeptEntity dept = deptInfra.getDeptById(request.deptId());
        if (dept == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
        }
        if (dept.getStatus() != null && dept.getStatus() == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "所属部门已被禁用");
        }

        // 检查角色是否存在
        if (request.roleIds() == null || request.roleIds().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "至少需要分配一个角色");
        }
        for (Long roleId : request.roleIds()) {
            RolesEntity role = rolesInfra.getRoleById(roleId);
            if (role == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在: " + roleId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected UserResponse process(CreateUserRequest request) {
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

        // 批量分配角色
        for (Long roleId : request.roleIds()) {
            UserRolesEntity userRole = new UserRolesEntity();
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleId);
            userRolesInfra.assignRole(userRole);
        }

        // 返回完整信息
        return buildUserResponse(user);
    }

    private UserResponse buildUserResponse(UsersEntity user) {
        // 查询部门信息
        DeptEntity dept = deptInfra.getDeptById(user.getDeptId());
        String deptName = dept != null ? dept.getDeptName() : null;

        // 查询所有角色
        List<UserRolesEntity> userRoles = userRolesInfra.listByUserId(user.getId());
        List<UserResponse.RoleInfo> roles = userRoles.stream().map(ur -> {
            RolesEntity role = rolesInfra.getRoleById(ur.getRoleId());
            return role != null ? new UserResponse.RoleInfo(role.getId(), role.getRoleName(), role.getRoleCode()) : null;
        }).filter(java.util.Objects::nonNull).toList();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getDeptId(),
                deptName,
                roles,
                user.getStatus(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }
}
