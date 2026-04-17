package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * MCP 工具级授权实体类
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName("eh_mcp_tool_grant")
public class McpToolGrantEntity extends BaseEntity {

    /** 工具ID */
    private Long toolId;

    /** 授权类型：USER-用户 DEPT-部门 ROLE-角色 */
    private String grantType;

    /** 目标ID（user_id / dept_id / role_id） */
    private Long targetId;
}
