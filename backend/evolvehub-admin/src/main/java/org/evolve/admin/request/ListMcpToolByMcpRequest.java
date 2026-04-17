package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 按 MCP Server 查询工具列表请求
 */
public record ListMcpToolByMcpRequest(
        @NotNull(message = "MCP Server ID 不能为空") Long mcpConfigId) {
}
