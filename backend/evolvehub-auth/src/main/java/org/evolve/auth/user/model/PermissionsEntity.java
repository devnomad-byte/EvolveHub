package org.evolve.auth.user.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * @className PermissionsEntity
 * @description 功能权限实体类（菜单 / 按钮 / API 三级）
 * @author zhao
 * @date 2026/4/9 14:52
 * @version v1.0
 **/
@Getter
@Setter
@TableName("t_permission")
public class PermissionsEntity extends BaseEntity {

    /**
     * 父权限 ID（0 表示顶级）
     */
    private Long parentId;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 权限编码（唯一标识，如 user:list、kb:read、kb:write）
     */
    private String permCode;

    /**
     * 权限类型：MENU-菜单 / BUTTON-按钮 / API-接口
     */
    private String permType;

    /**
     * 前端路由路径（仅菜单类型有效）
     */
    private String path;

    /**
     * 菜单图标（仅菜单类型有效）
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 状态（0-禁用 1-正常）
     */
    private Integer status;
}
