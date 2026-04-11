package org.evolve.auth.api;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.auth.response.MenuTreeResponse;
import org.evolve.auth.service.MenuTreeManager;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理接口
 *
 * @author zhao devnomad-byte
 */
@RestController
public class MenuController {

    @Resource
    private MenuTreeManager menuTreeManager;

    /**
     * 获取当前用户的菜单树（所有登录用户）
     *
     * @return 菜单树
     */
    @GetMapping("/admin/system/menus")
    public Result<List<MenuTreeResponse>> getMenus() {
        // 检查是否登录
        StpUtil.checkLogin();

        // 获取当前用户的角色
        List<String> roles = StpUtil.getRoleList();

        // 普通员工（USER）不能访问后台管理，返回空菜单
        if (roles.contains("USER")) {
            return Result.ok(List.of());
        }

        // 部门负责人（DEPT_HEAD）和高层领导（LEADER）不能访问后台管理，返回空菜单
        if (roles.contains("DEPT_HEAD") || roles.contains("LEADER")) {
            return Result.ok(List.of());
        }

        // 超级管理员（SUPER_ADMIN）和普通管理员（ADMIN）可以访问后台管理
        return Result.ok(menuTreeManager.execute());
    }

    /**
     * 获取用户界面菜单（所有登录用户）
     *
     * @return 空列表（用户界面无需菜单，由前端路由控制）
     */
    @GetMapping("/app/menus")
    public Result<List<MenuTreeResponse>> getAppMenus() {
        // 检查是否登录
        StpUtil.checkLogin();

        // 用户界面无需菜单，由前端路由控制
        return Result.ok(List.of());
    }
}
