package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 资源授权实体类
 * <p>
 * 记录管理员将系统级资源授权给指定用户/部门/角色的关系。
 * userId / deptId / roleId 至少填写一个。
 * </p>
 *
 * @author zhao
 * @version v1.1
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName("eh_resource_grant")
public class ResourceGrantEntity extends BaseEntity {

    /**
     * 被授权用户 ID（USER 级别授权必填）
     */
    private Long userId;

    /**
     * 被授权部门 ID（DEPT 级别授权必填）
     */
    private Long deptId;

    /**
     * 被授权角色 ID（ROLE 级别授权必填）
     */
    private Long roleId;

    /**
     * 资源类型：MODEL / SKILL / MCP
     */
    private String resourceType;

    /**
     * 资源 ID（指向具体资源表的主键）
     */
    private Long resourceId;
}
