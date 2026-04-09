package org.evolve.auth.user.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * @className RolesEntity
 * @description 角色实体类，dataScope 控制该角色的数据可见范围
 * @author zhao
 * @date 2026/4/9 14:48
 * @version v1.0
 **/
@Getter
@Setter
@TableName("t_role")
public class RolesEntity extends BaseEntity {

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码（唯一，如 ROLE_ADMIN / ROLE_USER）
     */
    private String roleCode;

    /**
     * 数据权限范围：
     * 1-全部数据（可看所有部门、所有知识库）
     * 2-本部门及子部门数据
     * 3-仅本部门数据
     * 4-仅本人数据
     * 5-自定义部门数据（关联 t_role_data_scope 表指定可见部门列表）
     */
    private Integer dataScope;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 状态（0-禁用 1-正常）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
