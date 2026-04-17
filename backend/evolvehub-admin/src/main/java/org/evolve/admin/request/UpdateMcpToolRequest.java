package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新 MCP 工具请求
 */
public record UpdateMcpToolRequest(
        @NotNull(message = "工具ID不能为空") Long id,
        String name,
        String description,
        String inputSchema,
        String riskLevel,
        String toolScope,
        Long deptId,
        Long ownerId,
        Integer enabled) {
}
