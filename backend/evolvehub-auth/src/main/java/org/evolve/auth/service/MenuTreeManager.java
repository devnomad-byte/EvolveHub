package org.evolve.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.auth.response.MenuTreeResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.PermissionsInfra;
import org.evolve.common.infra.RolePermissionsInfra;
import org.evolve.common.infra.RolesInfra;
import org.evolve.common.infra.UserRolesInfra;
import org.evolve.common.model.PermissionsEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询菜单树业务处理器
 *
 * @author zhao
 */
@Service
public class MenuTreeManager extends BaseManager<Void, List<MenuTreeResponse>> {

    @Resource
    private PermissionsInfra permissionsInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Resource
    private RolePermissionsInfra rolePermissionsInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Override
    protected void check(Void request) {
        // 检查是否登录
        StpUtil.checkLogin();
    }

    @Override
    protected List<MenuTreeResponse> process(Void request) {
        // 获取当前用户 ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询用户的角色
        List<String> roleCodes = StpUtil.getRoleList();

        // 如果是超级管理员，返回全部菜单
        if (roleCodes.contains("SUPER_ADMIN")) {
            List<PermissionsEntity> allMenus = permissionsInfra.listMenus();
            return buildMenuTree(allMenus, 0L);
        }

        // 普通用户，查询角色对应的菜单
        List<Long> roleIds = userRolesInfra.listByUserId(userId).stream()
                .map(ur -> ur.getRoleId())
                .collect(Collectors.toList());

        // 查询角色对应的权限 ID
        List<Long> permissionIds = roleIds.stream()
                .flatMap(roleId -> rolePermissionsInfra.listByRoleId(roleId).stream())
                .map(rp -> rp.getPermissionId())
                .distinct()
                .collect(Collectors.toList());

        // 查询菜单详情
        List<PermissionsEntity> menus = permissionIds.stream()
                .map(permissionId -> permissionsInfra.getById(permissionId))
                .filter(p -> p != null && "MENU".equals(p.getPermType()))
                .collect(Collectors.toList());

        // 构建菜单树
        return buildMenuTree(menus, 0L);
    }

    /**
     * 构建菜单树
     *
     * @param menus    所有菜单列表
     * @param parentId 父菜单 ID
     * @return 菜单树
     */
    private List<MenuTreeResponse> buildMenuTree(List<PermissionsEntity> menus, Long parentId) {
        List<MenuTreeResponse> tree = new ArrayList<>();

        for (PermissionsEntity menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                // 递归查找子菜单
                List<MenuTreeResponse> children = buildMenuTree(menus, menu.getId());
                MenuTreeResponse menuResponse = new MenuTreeResponse(
                        menu.getId(),
                        menu.getParentId(),
                        menu.getPermName(),
                        menu.getPermCode(),
                        menu.getPermType(),
                        menu.getPath(),
                        menu.getIcon(),
                        menu.getSort(),
                        menu.getStatus(),
                        children
                );
                tree.add(menuResponse);
            }
        }

        // 按 sort 排序
        tree.sort((a, b) -> {
            if (a.sort() == null) return 1;
            if (b.sort() == null) return -1;
            return a.sort().compareTo(b.sort());
        });

        return tree;
    }
}
