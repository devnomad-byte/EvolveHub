package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import org.evolve.admin.service.McpHeartbeatService;
import org.evolve.admin.service.McpInstanceService;
import org.evolve.admin.service.McpToolDiscoveryService;
import org.evolve.domain.resource.infra.McpToolInfra;
import org.evolve.domain.resource.model.McpInstanceEntity;
import org.evolve.domain.resource.model.McpToolEntity;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MCP 实例管理控制器
 *
 * 提供 MCP Server 的启动、停止、重启、状态查询等接口
 *
 * @author devnomad-byte
 * @version v1.0
 * @date 2026/4/15
 */
@RestController
@RequestMapping("/mcp-instance")
public class McpInstanceController {

    @Resource
    private McpInstanceService mcpInstanceService;

    @Resource
    private McpHeartbeatService mcpHeartbeatService;

    @Resource
    private McpToolDiscoveryService mcpToolDiscoveryService;

    @Resource
    private McpToolInfra mcpToolInfra;

    /**
     * 启动 MCP Server
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/{configId}/start")
    public Result<McpInstanceEntity> start(@PathVariable Long configId) {
        McpInstanceEntity instance = mcpInstanceService.startMcp(configId);
        return Result.ok(instance);
    }

    /**
     * 停止 MCP Server
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/{configId}/stop")
    public Result<Void> stop(@PathVariable Long configId) {
        mcpInstanceService.stopMcp(configId);
        return Result.ok();
    }

    /**
     * 重启 MCP Server
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/{configId}/restart")
    public Result<McpInstanceEntity> restart(@PathVariable Long configId) {
        McpInstanceEntity instance = mcpInstanceService.restartMcp(configId);
        return Result.ok(instance);
    }

    /**
     * 获取 MCP 运行状态
     */
    @SaCheckRole("SUPER_ADMIN")
    @GetMapping("/{configId}/status")
    public Result<McpInstanceEntity> status(@PathVariable Long configId) {
        McpInstanceEntity instance = mcpInstanceService.getStatus(configId);
        if (instance == null) {
            return Result.ok();
        }
        return Result.ok(instance);
    }

    /**
     * 触发心跳检测
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/{configId}/heartbeat")
    public Result<Boolean> heartbeat(@PathVariable Long configId) {
        boolean healthy = mcpHeartbeatService.checkInstance(configId);
        return Result.ok(healthy);
    }

    /**
     * 触发工具发现
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/{configId}/discover")
    public Result<List<McpToolEntity>> discover(@PathVariable Long configId) {
        McpSyncClient client = mcpInstanceService.getMcpClient(configId);
        if (client == null) {
            return Result.fail("MCP Server 未运行");
        }

        List<McpToolEntity> tools = mcpToolDiscoveryService.discoverTools(configId, client);

        // 保存到数据库
        mcpToolDiscoveryService.deleteToolsByMcpConfigId(configId);
        mcpToolDiscoveryService.saveTools(tools);

        return Result.ok(tools);
    }

    /**
     * 获取 MCP 工具列表
     */
    @GetMapping("/{configId}/tools")
    public Result<List<McpToolEntity>> listTools(@PathVariable Long configId) {
        List<McpToolEntity> tools = mcpToolInfra.listByMcpConfigId(configId);
        return Result.ok(tools);
    }

    /**
     * 获取所有运行中的实例
     */
    @SaCheckRole("SUPER_ADMIN")
    @GetMapping("/running")
    public Result<List<McpInstanceEntity>> listRunning() {
        List<McpInstanceEntity> instances = mcpInstanceService.getAllRunningInstances();
        return Result.ok(instances);
    }
}
