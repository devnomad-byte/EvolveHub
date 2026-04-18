package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.AddSecurityScannerWhitelistRequest;
import org.evolve.admin.request.UpdateSecurityScannerConfigRequest;
import org.evolve.admin.response.SecurityScannerBlockedHistoryResponse;
import org.evolve.admin.response.SecurityScannerConfigResponse;
import org.evolve.admin.response.SecurityScannerWhitelistResponse;
import org.evolve.admin.service.SecurityScannerBlockedHistoryDeleteManager;
import org.evolve.admin.service.SecurityScannerBlockedHistoryListManager;
import org.evolve.admin.service.SecurityScannerConfigGetManager;
import org.evolve.admin.service.SecurityScannerConfigUpdateManager;
import org.evolve.admin.service.SecurityScannerWhitelistAddManager;
import org.evolve.admin.service.SecurityScannerWhitelistDeleteManager;
import org.evolve.admin.service.SecurityScannerWhitelistListManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Security Scanner 安全扫描管理
 */
@RestController
@RequestMapping("/security-scanner")
public class SecurityScannerController {

    @Resource
    private SecurityScannerConfigGetManager configGetManager;

    @Resource
    private SecurityScannerConfigUpdateManager configUpdateManager;

    @Resource
    private SecurityScannerWhitelistListManager whitelistListManager;

    @Resource
    private SecurityScannerWhitelistAddManager whitelistAddManager;

    @Resource
    private SecurityScannerWhitelistDeleteManager whitelistDeleteManager;

    @Resource
    private SecurityScannerBlockedHistoryListManager historyListManager;

    @Resource
    private SecurityScannerBlockedHistoryDeleteManager historyDeleteManager;

    /**
     * 获取全局配置
     */
    @SaCheckPermission("security-scanner:config")
    @GetMapping("/config")
    public Result<SecurityScannerConfigResponse> getConfig() {
        return Result.ok(configGetManager.execute(null));
    }

    /**
     * 更新全局配置
     */
    @SaCheckPermission("security-scanner:manage")
    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody @Valid UpdateSecurityScannerConfigRequest request) {
        configUpdateManager.execute(request);
        return Result.ok();
    }

    /**
     * 获取白名单列表
     */
    @SaCheckPermission("security-scanner:whitelist")
    @GetMapping("/whitelist")
    public Result<List<SecurityScannerWhitelistResponse>> listWhitelist() {
        return Result.ok(whitelistListManager.execute(null));
    }

    /**
     * 添加白名单
     */
    @SaCheckPermission("security-scanner:whitelist:add")
    @PostMapping("/whitelist")
    public Result<Long> addWhitelist(@RequestBody @Valid AddSecurityScannerWhitelistRequest request) {
        return Result.ok(whitelistAddManager.execute(request));
    }

    /**
     * 删除白名单
     */
    @SaCheckPermission("security-scanner:whitelist:delete")
    @DeleteMapping("/whitelist/{id}")
    public Result<Void> deleteWhitelist(@PathVariable Long id) {
        whitelistDeleteManager.execute(id);
        return Result.ok();
    }

    /**
     * 获取阻断历史
     */
    @SaCheckPermission("security-scanner:history")
    @GetMapping("/history")
    public Result<PageResponse<SecurityScannerBlockedHistoryResponse>> listHistory(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId) {
        return Result.ok(historyListManager.execute(
                SecurityScannerBlockedHistoryListManager.Request.builder()
                        .action(action)
                        .userId(userId)
                        .pageNum(pageNum)
                        .pageSize(pageSize)
                        .build()));
    }

    /**
     * 清除所有历史记录
     */
    @SaCheckPermission("security-scanner:history:delete")
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        historyDeleteManager.execute(null);
        return Result.ok();
    }
}
