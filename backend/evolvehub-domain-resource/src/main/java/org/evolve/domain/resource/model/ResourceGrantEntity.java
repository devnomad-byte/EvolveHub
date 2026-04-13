package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 资源授权实体类
 * <p>
 * 记录管理员将系统级资源授权给指定用户的关系。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Getter
@Setter
@TableName("eh_resource_grant")
public class ResourceGrantEntity extends BaseEntity {

    /**
     * 被授权用户 ID
     */
    private Long userId;

    /**
     * 资源类型：MODEL / SKILL / MCP
     */
    private String resourceType;

    /**
     * 资源 ID（指向具体资源表的主键）
     */
    private Long resourceId;
}
