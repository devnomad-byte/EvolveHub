package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.evolve.common.infra.*;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.model.RoleDataScopeEntity;
import org.evolve.common.model.RolesEntity;
import org.evolve.common.model.UserRolesEntity;
import org.evolve.common.model.UsersEntity;
import org.evolve.common.base.CurrentUserHolder;
import org.evolve.common.datascope.DataScopeContext;
import org.evolve.common.datascope.DataScopeContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据权限 Web 拦截器
 * <p>
 * 在每次请求进入时，根据当前登录用户的角色 dataScope 计算可见部门集合，
 * 放入 {@link DataScopeContextHolder}，供 MyBatis 拦截器使用。
 * 请求结束后自动清除 ThreadLocal，防止内存泄漏。
 * </p>
 * <p>
 * 多角色策略：取所有角色中最大权限（最小 scopeType 值，1=全部 > 其他）。
 * </p>
 *
 * @author zhao
 */
@Component
public class DataScopeWebInterceptor implements HandlerInterceptor {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private RoleDataScopeInfra roleDataScopeInfra;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!StpUtil.isLogin()) {
            return true;
        }

        long userId = StpUtil.getLoginIdAsLong();
        CurrentUserHolder.set(userId);
        UsersEntity user = usersInfra.getUserById(userId);
        if (user == null) {
            return true;
        }

        List<UserRolesEntity> userRoles = userRolesInfra.listByUserId(userId);
        if (userRoles.isEmpty()) {
            DataScopeContextHolder.set(DataScopeContext.builder()
                    .userId(userId)
                    .deptId(user.getDeptId())
                    .scopeType(4)
                    .build());
            return true;
        }

        int finalScopeType = Integer.MAX_VALUE;
        Set<Long> allVisibleDeptIds = new HashSet<>();

        for (UserRolesEntity userRole : userRoles) {
            RolesEntity role = rolesInfra.getRoleById(userRole.getRoleId());
            if (role == null || role.getStatus() != 1) {
                continue;
            }

            int scopeType = role.getDataScope();

            if (scopeType == 1) {
                DataScopeContextHolder.set(DataScopeContext.builder()
                        .userId(userId)
                        .deptId(user.getDeptId())
                        .scopeType(1)
                        .build());
                return true;
            }

            if (scopeType < finalScopeType) {
                finalScopeType = scopeType;
            }

            switch (scopeType) {
                case 2 -> {
                    allVisibleDeptIds.add(user.getDeptId());
                    collectChildDeptIds(user.getDeptId(), allVisibleDeptIds);
                }
                case 3 -> {
                    allVisibleDeptIds.add(user.getDeptId());
                }
                case 5 -> {
                    List<RoleDataScopeEntity> scopes = roleDataScopeInfra.listByRoleId(role.getId());
                    for (RoleDataScopeEntity scope : scopes) {
                        allVisibleDeptIds.add(scope.getDeptId());
                    }
                }
            }
        }

        DataScopeContextHolder.set(DataScopeContext.builder()
                .userId(userId)
                .deptId(user.getDeptId())
                .scopeType(finalScopeType)
                .visibleDeptIds(allVisibleDeptIds.isEmpty() ? null : allVisibleDeptIds)
                .build());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        DataScopeContextHolder.clear();
        CurrentUserHolder.clear();
    }

    private void collectChildDeptIds(Long parentId, Set<Long> collectedIds) {
        List<DeptEntity> children = deptInfra.lambdaQuery()
                .eq(DeptEntity::getParentId, parentId)
                .eq(DeptEntity::getStatus, 1)
                .list();
        for (DeptEntity child : children) {
            collectedIds.add(child.getId());
            collectChildDeptIds(child.getId(), collectedIds);
        }
    }
}
