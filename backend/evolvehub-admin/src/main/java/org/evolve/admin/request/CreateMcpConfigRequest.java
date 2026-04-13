package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建系统级 MCP 配置请求
 *
 * @param name        服务名称
 * @param description 服务描述
 * @param serverUrl   服务地址
 * @param protocol    传输协议（stdio / sse）
 * @param config      配置信息（JSON 字符串）
 * @param enabled     启用状态（1=启用 0=禁用）
 */
public record CreateMcpConfigRequest(
        @NotBlank(message = "服务名称不能为空") String name,
        String description,
        @NotBlank(message = "服务地址不能为空") String serverUrl,
        String protocol,
        String config,
        @NotNull(message = "启用状态不能为空") Integer enabled) {
}
