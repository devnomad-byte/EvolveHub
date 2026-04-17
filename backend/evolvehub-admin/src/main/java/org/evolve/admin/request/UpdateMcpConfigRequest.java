package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新 MCP 配置请求
 *
 * @param id           MCP 配置 ID（必填）
 * @param name         服务名称
 * @param description  服务描述
 * @param transportType 传输类型
 * @param serverUrl    服务器地址
 * @param packagePath  zip 包路径
 * @param command      启动命令
 * @param args         命令参数
 * @param env          环境变量
 * @param protocol     协议类型
 * @param config       配置信息
 * @param enabled      启用状态
 * @param scope        资源范围
 * @param deptId       部门ID
 */
public record UpdateMcpConfigRequest(
        @NotNull(message = "MCP配置ID不能为空") Long id,
        String name,
        String description,
        String transportType,
        String serverUrl,
        String packagePath,
        String command,
        String args,
        String env,
        String protocol,
        String config,
        Integer enabled,
        String scope,
        Long deptId) {
}
