package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.UserResponse;
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

import java.util.List;

/**
 * 查询用户详情业务处理器
 *
 * @author zhao
 */
@Service
public class GetUserManager extends BaseManager<Long, UserResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户ID不能为空");
        }
    }

    @Override
    protected UserResponse process(Long id) {
        UsersEntity user = usersInfra.getUserById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
        return buildUserResponse(user);
    }

    private UserResponse buildUserResponse(UsersEntity user) {
        // 查询部门信息
        DeptEntity dept = deptInfra.getDeptById(user.getDeptId());
        String deptName = dept != null ? dept.getDeptName() : null;

        // 查询角色信息
        UserRolesEntity userRole = userRolesInfra.listByUserId(user.getId()).stream().findFirst().orElse(null);
        RolesEntity role = null;
        if (userRole != null) {
            role = rolesInfra.getRoleById(userRole.getRoleId());
        }

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
