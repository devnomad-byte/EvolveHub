package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建 MCP 工具授权请求
 */
public record CreateMcpToolGrantRequest(
        @NotNull(message = "工具ID不能为空") Long toolId,
        @NotBlank(message = "授权类型不能为空") String grantType,
        @NotNull(message = "目标ID不能为空") Long targetId) {
}
