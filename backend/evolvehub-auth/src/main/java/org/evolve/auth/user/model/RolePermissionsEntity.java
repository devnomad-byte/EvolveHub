package org.evolve.auth.user.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * @className RolePermissionsEntity
 * @description 角色-权限关联实体类
 * @author zhao
 * @date 2026/4/9 14:52
 * @version v1.0
 **/
@Getter
@Setter
@TableName("t_role_permission")
public class RolePermissionsEntity extends BaseEntity {

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 权限 ID
     */
    private Long permissionId;
}
