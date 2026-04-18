package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateToolGuardRuleRequest;
import org.evolve.admin.request.UpdateToolGuardConfigRequest;
import org.evolve.admin.request.UpdateToolGuardRuleRequest;
import org.evolve.admin.response.ToolGuardConfigResponse;
import org.evolve.admin.response.ToolGuardHistoryResponse;
import org.evolve.admin.response.ToolGuardRuleResponse;
import org.evolve.admin.service.ToolGuardConfigGetManager;
import org.evolve.admin.service.ToolGuardConfigUpdateManager;
import org.evolve.admin.service.ToolGuardHistoryDeleteManager;
import org.evolve.admin.service.ToolGuardHistoryListManager;
import org.evolve.admin.service.ToolGuardRuleCreateManager;
import org.evolve.admin.service.ToolGuardRuleDeleteManager;
import org.evolve.admin.service.ToolGuardRuleListManager;
import org.evolve.admin.service.ToolGuardRuleToggleManager;
import org.evolve.admin.service.ToolGuardRuleUpdateManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Tool Guard 工具守卫管理
 */
@RestController
@RequestMapping("/tool-guard")
public class ToolGuardController {

    @Resource
    private ToolGuardRuleListManager toolGuardRuleListManager;

    @Resource
    private ToolGuardRuleCreateManager toolGuardRuleCreateManager;

    @Resource
    private ToolGuardRuleUpdateManager toolGuardRuleUpdateManager;

    @Resource
    private ToolGuardRuleDeleteManager toolGuardRuleDeleteManager;

    @Resource
    private ToolGuardRuleToggleManager toolGuardRuleToggleManager;

    @Resource
    private ToolGuardConfigGetManager toolGuardConfigGetManager;

    @Resource
    private ToolGuardConfigUpdateManager toolGuardConfigUpdateManager;

    @Resource
    private ToolGuardHistoryListManager toolGuardHistoryListManager;

    @Resource
    private ToolGuardHistoryDeleteManager toolGuardHistoryDeleteManager;

    /**
     * 获取所有规则（含内置）
     */
    @SaCheckPermission("tool-guard:list")
    @GetMapping("/rules")
    public Result<List<ToolGuardRuleResponse>> listRules() {
        return Result.ok(toolGuardRuleListManager.execute(null));
    }

    /**
     * 创建自定义规则
     */
    @SaCheckPermission("tool-guard:create")
    @PostMapping("/rules")
    public Result<Long> createRule(@RequestBody @Valid CreateToolGuardRuleRequest request) {
        return Result.ok(toolGuardRuleCreateManager.execute(request));
    }

    /**
     * 更新规则
     */
    @SaCheckPermission("tool-guard:update")
    @PutMapping("/rules")
    public Result<Void> updateRule(@RequestBody @Valid UpdateToolGuardRuleRequest request) {
        toolGuardRuleUpdateManager.execute(request);
        return Result.ok();
    }

    /**
     * 删除规则（仅自定义规则）
     */
    @SaCheckPermission("tool-guard:delete")
    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        toolGuardRuleDeleteManager.execute(id);
        return Result.ok();
    }

    /**
     * 启用/禁用规则
     */
    @SaCheckPermission("tool-guard:update")
    @PutMapping("/rules/{id}/toggle")
    public Result<Void> toggleRule(@PathVariable Long id) {
        toolGuardRuleToggleManager.execute(id);
        return Result.ok();
    }

    /**
     * 获取全局配置
     */
    @SaCheckPermission("tool-guard:manage")
    @GetMapping("/config")
    public Result<ToolGuardConfigResponse> getConfig() {
        return Result.ok(toolGuardConfigGetManager.execute(null));
    }

    /**
     * 更新全局配置
     */
    @SaCheckPermission("tool-guard:manage")
    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody @Valid UpdateToolGuardConfigRequest request) {
        toolGuardConfigUpdateManager.execute(request);
        return Result.ok();
    }

    /**
     * 获取阻断历史
     */
    @SaCheckPermission("tool-guard:history")
    @GetMapping("/history")
    public Result<PageResponse<ToolGuardHistoryResponse>> listHistory(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Long userId) {
        return Result.ok(toolGuardHistoryListManager.execute(
                new ToolGuardHistoryListManager.Request(severity, userId, pageNum, pageSize)));
    }

    /**
     * 清除所有历史记录
     */
    @SaCheckPermission("tool-guard:history:delete")
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        toolGuardHistoryDeleteManager.execute(null);
        return Result.ok();
    }
}
