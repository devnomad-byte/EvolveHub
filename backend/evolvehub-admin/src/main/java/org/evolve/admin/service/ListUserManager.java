package org.evolve.admin.service;

import jakarta.annotation.Resource;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询用户列表业务处理器
 *
 * @author zhao
 */
@Service
public class ListUserManager extends BaseManager<Void, List<UserResponse>> {

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
        List<UsersEntity> users = usersInfra.list();
        return users.stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse buildUserResponse(UsersEntity user) {
        // 查询部门信息
        DeptEntity dept = deptInfra.getDeptById(user.getDeptId());
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
