package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateUserRequest;
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
 * 更新用户业务处理器
 *
 * @author zhao
 */
@Service
public class UpdateUserManager extends BaseManager<UpdateUserRequest, UserResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(UpdateUserRequest request) {
        UsersEntity user = usersInfra.getUserById(request.id());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }

        // 不能修改自己的状态和角色（安全考虑）
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(request.id())) {
            if (request.status() != null || request.roleIds() != null) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能修改自己的状态或角色");
            }
        }

        // 如果要修改部门，检查部门是否存在
        if (request.deptId() != null) {
            DeptEntity dept = deptInfra.getDeptById(request.deptId());
            if (dept == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
            }
        }

        // 如果要修改角色，检查角色是否存在
        if (request.roleIds() != null) {
            for (Long roleId : request.roleIds()) {
                RolesEntity role = rolesInfra.getRoleById(roleId);
                if (role == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在: " + roleId);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected UserResponse process(UpdateUserRequest request) {
        UsersEntity user = usersInfra.getUserById(request.id());

        // 更新用户信息
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setAvatar(request.avatar());
        if (request.deptId() != null) {
            user.setDeptId(request.deptId());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        usersInfra.updateUser(user);

        // 如果要更新角色
        if (request.roleIds() != null) {
            // 删除旧角色
            userRolesInfra.removeByUserId(user.getId());

            // 批量分配新角色
            for (Long roleId : request.roleIds()) {
                UserRolesEntity userRole = new UserRolesEntity();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRolesInfra.assignRole(userRole);
            }

            // 如果禁用了用户，强制下线
            if (request.status() != null && request.status() == 0) {
                StpUtil.kickout(user.getId());
            }
        }

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
