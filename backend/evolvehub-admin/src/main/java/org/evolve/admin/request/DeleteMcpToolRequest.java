package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 删除 MCP 工具请求
 */
public record DeleteMcpToolRequest(
        @NotNull(message = "工具ID不能为空") Long id) {
}
