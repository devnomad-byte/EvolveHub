package org.evolve.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询用户列表业务处理器
 *
 * @author zhao
 */
@Service
public class UserListManager extends BaseManager<Void, List<UserResponse>> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(Void request) {
        // 无需校验
    }

    @Override
    protected List<UserResponse> process(Void request) {
        // 查询所有用户
        List<UsersEntity> users = usersInfra.list();

        // 转换为响应对象
        return users.stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse buildUserResponse(UsersEntity user) {
        // 查询部门信息
        DeptEntity dept = deptInfra.getById(user.getDeptId());
        String deptName = dept != null ? dept.getDeptName() : null;

        // 查询角色信息
        List<UserRolesEntity> userRoles = userRolesInfra.listByUserId(user.getId());
        List<UserResponse.RoleInfo> roles = userRoles.stream()
                .map(ur -> {
                    RolesEntity role = rolesInfra.getRoleById(ur.getRoleId());
                    if (role != null) {
                        return new UserResponse.RoleInfo(role.getId(), role.getRoleName(), role.getRoleCode());
                    }
                    return null;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

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
