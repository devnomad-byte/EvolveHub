package org.evolve.domain.rbac.service;

import jakarta.annotation.Resource;
import lombok.Builder;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.infra.RoleDataScopeInfra;
import org.evolve.domain.rbac.infra.RolesInfra;
import org.evolve.domain.rbac.infra.UserRolesInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.RolesEntity;
import org.evolve.domain.rbac.model.UserRolesEntity;
import org.evolve.domain.rbac.model.UsersEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户数据权限服务
 *
 * @author zhao
 * @date 2026/4/18
 */
@Service
public class UserDataScopeService {

    @Resource
    private UsersInfra usersInfra;
    @Resource
    private UserRolesInfra userRolesInfra;
    @Resource
    private RolesInfra rolesInfra;
    @Resource
    private RoleDataScopeInfra roleDataScopeInfra;
    @Resource
    private DeptInfra deptInfra;

    @Builder
    public record DataScopeInfo(
            Integer dataScope,
            Long deptId,
            Set<Long> visibleDeptIds
    ) {}

    public DataScopeInfo getDataScopeInfo(Long userId) {
        UsersEntity user = usersInfra.getById(userId);
        if (user == null) {
            return DataScopeInfo.builder().dataScope(4).deptId(null).visibleDeptIds(null).build();
        }

        List<UserRolesEntity> userRoles = userRolesInfra.listByUserId(userId);
        if (userRoles == null || userRoles.isEmpty()) {
            return DataScopeInfo.builder().dataScope(4).deptId(user.getDeptId()).visibleDeptIds(null).build();
        }

        Integer minDataScope = null;
        for (UserRolesEntity userRole : userRoles) {
            RolesEntity role = rolesInfra.getById(userRole.getRoleId());
            if (role != null && role.getDataScope() != null) {
                if (minDataScope == null || role.getDataScope() < minDataScope) {
                    minDataScope = role.getDataScope();
                }
            }
        }
        if (minDataScope == null) {
            minDataScope = 4;
        }

        Set<Long> visibleDeptIds = null;
        if (minDataScope == 2 && user.getDeptId() != null) {
            List<Long> descendantDeptIds = deptInfra.getDescendantDeptIds(user.getDeptId());
            visibleDeptIds = new HashSet<>(descendantDeptIds);
        } else if (minDataScope == 5) {
            visibleDeptIds = loadVisibleDeptIds(userRoles);
        }

        return DataScopeInfo.builder()
                .dataScope(minDataScope)
                .deptId(user.getDeptId())
                .visibleDeptIds(visibleDeptIds)
                .build();
    }

    private Set<Long> loadVisibleDeptIds(List<UserRolesEntity> userRoles) {
        Set<Long> deptIds = new HashSet<>();
        for (UserRolesEntity userRole : userRoles) {
            var roleDataScopes = roleDataScopeInfra.listByRoleId(userRole.getRoleId());
            if (roleDataScopes != null) {
                for (var roleDataScope : roleDataScopes) {
                    if (roleDataScope.getDeptId() != null) {
                        deptIds.add(roleDataScope.getDeptId());
                    }
                }
            }
        }
        return deptIds.isEmpty() ? null : deptIds;
    }
}
