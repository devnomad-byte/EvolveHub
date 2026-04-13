package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.GenerateApiKeyRequest;
import org.evolve.admin.response.GenerateApiKeyResponse;
import org.evolve.admin.service.DisableApiKeyManager;
import org.evolve.admin.service.GenerateApiKeyManager;
import org.evolve.admin.service.ListApiKeyManager;
import org.evolve.admin.service.ResetApiKeyManager;
import org.evolve.common.model.ApiKeyEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * API Key 管理控制器
 * <p>
 * 管理员为用户生成、重置、禁用 API Key。
 * 所有接口仅超级管理员可操作。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@RestController
@RequestMapping("/api-key")
public class ApiKeyController {

    @Resource
    private GenerateApiKeyManager generateApiKeyManager;

    @Resource
    private ResetApiKeyManager resetApiKeyManager;

    @Resource
    private DisableApiKeyManager disableApiKeyManager;

    @Resource
    private ListApiKeyManager listApiKeyManager;

    /**
     * 为指定用户生成 API Key
     *
     * @param request 包含目标用户 ID
     * @return 生成的 API Key 信息
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/generate")
    public Result<GenerateApiKeyResponse> generate(@RequestBody @Valid GenerateApiKeyRequest request) {
        return Result.ok(generateApiKeyManager.execute(request));
    }

    /**
     * 重置指定用户的 API Key（旧 Key 立即失效）
     *
     * @param userId 用户 ID
     * @return 新的 API Key 信息
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/reset/{userId}")
    public Result<GenerateApiKeyResponse> reset(@PathVariable Long userId) {
        return Result.ok(resetApiKeyManager.execute(userId));
    }

    /**
     * 禁用指定用户的 API Key
     *
     * @param userId 用户 ID
     * @return 操作结果
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/disable/{userId}")
    public Result<Void> disable(@PathVariable Long userId) {
        disableApiKeyManager.execute(userId);
        return Result.ok();
    }

    /**
     * 分页查询所有用户的 API Key 列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页 API Key 列表
     */
    @SaCheckRole("SUPER_ADMIN")
    @GetMapping("/list")
    public Result<PageResponse<ApiKeyEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listApiKeyManager.execute(new PageRequest(pageNum, pageSize)));
    }
}
