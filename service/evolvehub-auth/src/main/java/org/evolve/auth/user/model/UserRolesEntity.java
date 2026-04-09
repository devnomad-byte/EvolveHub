package org.evolve.auth.user.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * @className UserRolesEntity
 * @description 用户-角色关联实体类
 * @author zhao
 * @date 2026/4/9 14:53
 * @version v1.0
 **/
@Getter
@Setter
@TableName("t_user_role")
public class UserRolesEntity extends BaseEntity {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 角色 ID
     */
    private Long roleId;
}
