package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.BatchUpdateAgentRuntimeConfigRequest;
import org.evolve.admin.request.UpdateAgentRuntimeConfigRequest;
import org.evolve.admin.response.AgentRuntimeConfigHistoryResponse;
import org.evolve.admin.response.AgentRuntimeConfigResponse;
import org.evolve.admin.service.AgentRuntimeConfigBatchUpdateManager;
import org.evolve.admin.service.AgentRuntimeConfigDefaultsManager;
import org.evolve.admin.service.AgentRuntimeConfigGetManager;
import org.evolve.admin.service.AgentRuntimeConfigHistoryListManager;
import org.evolve.admin.service.AgentRuntimeConfigUpdateManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent运行时配置管理
 */
@RestController
@RequestMapping("/runtime-config")
public class AgentRuntimeConfigController {

    @Resource
    private AgentRuntimeConfigGetManager agentRuntimeConfigGetManager;

    @Resource
    private AgentRuntimeConfigUpdateManager agentRuntimeConfigUpdateManager;

    @Resource
    private AgentRuntimeConfigBatchUpdateManager agentRuntimeConfigBatchUpdateManager;

    @Resource
    private AgentRuntimeConfigHistoryListManager agentRuntimeConfigHistoryListManager;

    @Resource
    private AgentRuntimeConfigDefaultsManager agentRuntimeConfigDefaultsManager;

    /**
     * 获取指定配置
     */
    @SaCheckPermission("agent-config:list")
    @GetMapping("/{configKey}")
    public Result<List<AgentRuntimeConfigResponse>> getConfig(@PathVariable String configKey) {
        return Result.ok(agentRuntimeConfigGetManager.execute(configKey));
    }

    /**
     * 获取所有配置
     */
    @SaCheckPermission("agent-config:list")
    @GetMapping("/all")
    public Result<List<AgentRuntimeConfigResponse>> getAllConfig() {
        return Result.ok(agentRuntimeConfigGetManager.execute(null));
    }

    /**
     * 更新指定配置
     */
    @SaCheckPermission("agent-config:update")
    @PutMapping("/{configKey}")
    public Result<Void> updateConfig(@PathVariable String configKey,
                                      @RequestBody @Valid UpdateAgentRuntimeConfigRequest request) {
        // 使用请求中的configKey（路径变量）而不是request中的
        UpdateAgentRuntimeConfigRequest fullRequest = new UpdateAgentRuntimeConfigRequest(
                configKey, request.configValue(), request.changeReason());
        agentRuntimeConfigUpdateManager.execute(fullRequest);
        return Result.ok();
    }

    /**
     * 批量更新配置
     */
    @SaCheckPermission("agent-config:update")
    @PutMapping("/batch")
    public Result<Void> batchUpdateConfig(@RequestBody @Valid BatchUpdateAgentRuntimeConfigRequest request) {
        agentRuntimeConfigBatchUpdateManager.execute(request);
        return Result.ok();
    }

    /**
     * 获取默认配置值
     */
    @SaCheckPermission("agent-config:list")
    @GetMapping("/defaults")
    public Result<List<AgentRuntimeConfigResponse>> getDefaults() {
        return Result.ok(agentRuntimeConfigDefaultsManager.execute(null));
    }

    /**
     * 获取配置变更历史
     */
    @SaCheckPermission("agent-config:list")
    @GetMapping("/history")
    public Result<PageResponse<AgentRuntimeConfigHistoryResponse>> getHistory(
            @RequestParam(required = false) String configKey,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(agentRuntimeConfigHistoryListManager.execute(
                new AgentRuntimeConfigHistoryListManager.Request(configKey, pageNum, pageSize)));
    }
}
