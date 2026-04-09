package org.evolve.auth.user.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * @className RoleDataScopeEntity
 * @description 角色自定义数据范围实体类（当 RolesEntity.dataScope=5 时生效，指定该角色可见的部门列表）
 * @author zhao
 * @date 2026/4/9
 * @version v1.0
 **/
@Getter
@Setter
@TableName("t_role_data_scope")
public class RoleDataScopeEntity extends BaseEntity {

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 可见部门 ID
     */
    private Long deptId;
}
