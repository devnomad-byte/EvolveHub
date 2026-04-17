package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建 MCP 工具请求
 */
public record CreateMcpToolRequest(
        @NotNull(message = "MCP Server ID 不能为空") Long mcpConfigId,
        @NotBlank(message = "工具名称不能为空") String name,
        String description,
        String inputSchema,
        String riskLevel,
        String toolScope,
        Long deptId,
        Long ownerId,
        Integer enabled) {
}
