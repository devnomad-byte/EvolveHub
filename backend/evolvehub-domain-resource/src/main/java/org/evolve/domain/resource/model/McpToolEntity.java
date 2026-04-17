package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * MCP 服务工具实体类
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName("eh_mcp_tool")
public class McpToolEntity extends BaseEntity {

    /** 所属 MCP Server ID */
    private Long mcpConfigId;

    /** 工具名称 */
    private String name;

    /** 工具描述 */
    private String description;

    /** 工具输入参数 JSON Schema */
    private String inputSchema;

    /** 风险等级：LOW-低 MEDIUM-中 HIGH-高 */
    private String riskLevel;

    /** 工具可见范围：SYSTEM-系统级 DEPT-部门级 USER-个人级 GRANT-授权访问 */
    private String toolScope;

    /** 部门ID，tool_scope=DEPT 时必填 */
    private Long deptId;

    /** 所有者ID，tool_scope=USER 时必填 */
    private Long ownerId;

    /** 工具开关：1-启用 0-禁用 */
    private Integer enabled;
}
