package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建 MCP 配置请求
 *
 * @param name          服务名称
 * @param description   服务描述
 * @param transportType 传输类型：UPLOADED-本地上传 REMOTE-远程URL
 * @param serverUrl     服务器地址（REMOTE 模式必填）
 * @param packagePath   zip 包存储路径（UPLOADED 模式）
 * @param command       启动命令（UPLOADED 模式）
 * @param args          命令参数（JSON 数组）
 * @param env           环境变量（JSON 对象）
 * @param protocol      协议类型（STDIO / SSE，仅兼容性保留）
 * @param config        配置信息（JSON 字符串）
 * @param enabled       启用状态（1=启用 0=禁用）
 * @param scope         资源范围：SYSTEM / DEPT / USER
 * @param deptId        部门ID（scope=DEPT 时必填）
 */
public record CreateMcpConfigRequest(
        @NotBlank(message = "服务名称不能为空") String name,
        String description,
        String transportType,
        String serverUrl,
        String packagePath,
        String command,
        String args,
        String env,
        String protocol,
        String config,
        @NotNull(message = "启用状态不能为空") Integer enabled,
        String scope,
        Long deptId) {
}
