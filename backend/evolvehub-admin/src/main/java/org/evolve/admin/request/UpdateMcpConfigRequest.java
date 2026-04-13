package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新系统级 MCP 配置请求
 *
 * @param id          MCP 配置 ID（必填）
 * @param name        服务名称
 * @param description 服务描述
 * @param serverUrl   服务地址
 * @param protocol    传输协议（stdio / sse）
 * @param config      配置信息（JSON 字符串）
 * @param enabled     启用状态（1=启用 0=禁用）
 */
public record UpdateMcpConfigRequest(
        @NotNull(message = "MCP配置ID不能为空") Long id,
        String name, String description, String serverUrl,
        String protocol, String config, Integer enabled) {
}
