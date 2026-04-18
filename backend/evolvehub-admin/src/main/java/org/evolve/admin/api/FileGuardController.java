package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateFileGuardRuleRequest;
import org.evolve.admin.request.UpdateFileGuardConfigRequest;
import org.evolve.admin.request.UpdateFileGuardRuleRequest;
import org.evolve.admin.response.FileGuardConfigResponse;
import org.evolve.admin.response.FileGuardRuleResponse;
import org.evolve.admin.service.*;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * File Guard 敏感文件保护管理
 */
@RestController
@RequestMapping("/file-guard")
public class FileGuardController {

    @Resource
    private FileGuardRuleListManager fileGuardRuleListManager;

    @Resource
    private FileGuardRuleCreateManager fileGuardRuleCreateManager;

    @Resource
    private FileGuardRuleUpdateManager fileGuardRuleUpdateManager;

    @Resource
    private FileGuardRuleDeleteManager fileGuardRuleDeleteManager;

    @Resource
    private FileGuardRuleToggleManager fileGuardRuleToggleManager;

    @Resource
    private FileGuardConfigGetManager fileGuardConfigGetManager;

    @Resource
    private FileGuardConfigUpdateManager fileGuardConfigUpdateManager;

    /**
     * 获取所有规则（含内置）
     */
    @SaCheckPermission("file-guard:list")
    @GetMapping("/rules")
    public Result<List<FileGuardRuleResponse>> listRules() {
        return Result.ok(fileGuardRuleListManager.execute(null));
    }

    /**
     * 创建自定义规则
     */
    @SaCheckPermission("file-guard:create")
    @PostMapping("/rules")
    public Result<Long> createRule(@RequestBody @Valid CreateFileGuardRuleRequest request) {
        return Result.ok(fileGuardRuleCreateManager.execute(request));
    }

    /**
     * 更新规则
     */
    @SaCheckPermission("file-guard:update")
    @PutMapping("/rules")
    public Result<Void> updateRule(@RequestBody @Valid UpdateFileGuardRuleRequest request) {
        fileGuardRuleUpdateManager.execute(request);
        return Result.ok();
    }

    /**
     * 删除规则（仅自定义规则）
     */
    @SaCheckPermission("file-guard:delete")
    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        fileGuardRuleDeleteManager.execute(id);
        return Result.ok();
    }

    /**
     * 启用/禁用规则
     */
    @SaCheckPermission("file-guard:update")
    @PutMapping("/rules/{id}/toggle")
    public Result<Void> toggleRule(@PathVariable Long id) {
        fileGuardRuleToggleManager.execute(id);
        return Result.ok();
    }

    /**
     * 获取全局配置
     */
    @SaCheckPermission("file-guard:manage")
    @GetMapping("/config")
    public Result<FileGuardConfigResponse> getConfig() {
        return Result.ok(fileGuardConfigGetManager.execute(null));
    }

    /**
     * 更新全局配置
     */
    @SaCheckPermission("file-guard:manage")
    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody @Valid UpdateFileGuardConfigRequest request) {
        fileGuardConfigUpdateManager.execute(request);
        return Result.ok();
    }
}
