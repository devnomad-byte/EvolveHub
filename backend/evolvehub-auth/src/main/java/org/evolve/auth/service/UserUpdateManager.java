package org.evolve.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.auth.request.UserUpdateRequest;
import org.evolve.auth.response.UserResponse;
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
public class UserUpdateManager extends BaseManager<UserUpdateRequest, UserResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(UserUpdateRequest request) {
        // 检查用户是否存在
        UsersEntity user = usersInfra.getById(request.id());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }

        // 不能修改自己的状态和角色（安全考虑）
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(request.id())) {
            if (request.status() != null || request.roleId() != null) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能修改自己的状态或角色");
            }
        }

        // 如果要修改部门，检查部门是否存在
        if (request.deptId() != null) {
            DeptEntity dept = deptInfra.getById(request.deptId());
            if (dept == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
            }
        }

        // 如果要修改角色，检查角色是否存在
        if (request.roleId() != null) {
            RolesEntity role = rolesInfra.getRoleById(request.roleId());
            if (role == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected UserResponse process(UserUpdateRequest request) {
        // 查询用户
        UsersEntity user = usersInfra.getById(request.id());

        // 更新用户信息
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        if (request.deptId() != null) {
            user.setDeptId(request.deptId());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        usersInfra.updateUser(user);

        // 如果要更新角色
        if (request.roleId() != null) {
            // 删除旧角色
            userRolesInfra.removeByUserId(user.getId());

            // 分配新角色
            UserRolesEntity userRole = new UserRolesEntity();
            userRole.setUserId(user.getId());
            userRole.setRoleId(request.roleId());
            userRolesInfra.assignRole(userRole);

            // 如果禁用了用户，强制下线
            if (request.status() != null && request.status() == 0) {
                StpUtil.kickout(user.getId());
            }
        }

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
