package org.evolve.auth.response;

import java.util.List;

/**
 * 菜单树响应
 *
 * @author zhao
 */
public record MenuTreeResponse(

        /**
         * 菜单 ID
         */
        Long id,

        /**
         * 父菜单 ID
         */
        Long parentId,

        /**
         * 菜单名称
         */
        String menuName,

        /**
         * 权限编码
         */
        String permCode,

        /**
         * 菜单类型（MENU-菜单, BUTTON-按钮）
         */
        String menuType,

        /**
         * 路由地址
         */
        String path,

        /**
         * 菜单图标
         */
        String icon,

        /**
         * 排序号
         */
        Integer sort,

        /**
         * 显示状态（0-隐藏 1-显示）
         */
        Integer status,

        /**
         * 子菜单列表
         */
        List<MenuTreeResponse> children
) {
}
