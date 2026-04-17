package org.evolve.admin.response;

import lombok.Getter;
import lombok.Setter;

/**
 * MCP 工具响应
 */
@Getter
@Setter
public class McpToolResponse {
    private Long id;
    private Long mcpConfigId;
    private String name;
    private String description;
    private String inputSchema;
    private String riskLevel;
    private String toolScope;
    private Long deptId;
    private Long ownerId;
    private Integer enabled;
}
