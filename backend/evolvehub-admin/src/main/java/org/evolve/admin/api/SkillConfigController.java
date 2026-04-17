package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateSkillConfigRequest;
import org.evolve.admin.request.UpdateSkillConfigRequest;
import org.evolve.admin.request.UpdateSkillContentRequest;
import org.evolve.admin.response.CreateSkillConfigResponse;
import org.evolve.admin.response.SecurityScanResult;
import org.evolve.admin.response.UpdateSkillConfigResponse;
import org.evolve.admin.service.*;
import org.evolve.admin.utils.FileStorageUtil;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * 技能配置管理控制器
 */
@RestController
@RequestMapping("/skill-config")
public class SkillConfigController {

    @Resource
    private CreateSkillConfigManager createSkillConfigManager;

    @Resource
    private UpdateSkillConfigManager updateSkillConfigManager;

    @Resource
    private GetSkillConfigManager getSkillConfigManager;

    @Resource
    private ListSkillConfigManager listSkillConfigManager;

    @Resource
    private DeleteSkillConfigManager deleteSkillConfigManager;

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Resource
    private FileStorageUtil fileStorageUtil;

    @Resource
    private SecurityScanner securityScanner;

    /**
     * 创建技能配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/create")
    public Result<CreateSkillConfigResponse> create(@RequestBody @Valid CreateSkillConfigRequest request) {
        return Result.ok(createSkillConfigManager.execute(request));
    }

    /**
     * 根据 ID 查询技能配置详情
     */
    @GetMapping("/{id}")
    public Result<SkillConfigEntity> getById(@PathVariable Long id) {
        return Result.ok(getSkillConfigManager.execute(id));
    }

    /**
     * 分页查询技能配置列表
     */
    @GetMapping("/list")
    public Result<PageResponse<SkillConfigEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listSkillConfigManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新技能配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PutMapping("/update")
    public Result<UpdateSkillConfigResponse> update(@RequestBody @Valid UpdateSkillConfigRequest request) {
        return Result.ok(updateSkillConfigManager.execute(request));
    }

    /**
     * 删除技能配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteSkillConfigManager.execute(id);
        return Result.ok();
    }

    /**
     * 启用/禁用技能配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/{id}/toggle")
    public Result<UpdateSkillConfigResponse> toggle(@PathVariable Long id) {
        SkillConfigEntity existing = getSkillConfigManager.execute(id);
        UpdateSkillConfigRequest request = new UpdateSkillConfigRequest(
                id, null, null, null, null, null, null, null, null,
                existing.getEnabled() == 1 ? 0 : 1,
                existing.getScope(), existing.getDeptId()
        );
        return Result.ok(updateSkillConfigManager.execute(request));
    }

    /**
     * 更新技能内容（在线编辑 SKILL.md）
     * 写入数据库 + 文件存储
     */
    @SaCheckRole("SUPER_ADMIN")
    @PutMapping("/{id}/content")
    public Result<?> updateContent(@PathVariable Long id,
                                       @RequestBody UpdateSkillContentRequest request,
                                       @RequestParam(value = "skipScan", defaultValue = "false") boolean skipScan) {
        // 0. 安全扫描（skipScan 仅允许跳过 MEDIUM/LOW，CRITICAL/HIGH 始终阻断）
        if (!skipScan) {
            SecurityScanResult scanResult = securityScanner.scanText(request.content(), "SKILL.md");
            if (!scanResult.isPassed()) {
                return Result.fail(4001, "安全扫描未通过", scanResult);
            }
        } else {
            SecurityScanResult scanResult = securityScanner.scanText(request.content(), "SKILL.md");
            if (!scanResult.isPassed() && !securityScanner.canBypass(scanResult)) {
                return Result.fail(4001, "安全扫描未通过（含严重/高危问题，不可跳过）", scanResult);
            }
        }

        // 1. 获取工作区路径
        String workspacePath = skillConfigInfra.getWorkspacePath(id);

        // 2. 写入文件存储
        String skillMdKey = workspacePath + "SKILL.md";
        byte[] contentBytes = request.content().getBytes(StandardCharsets.UTF_8);
        fileStorageUtil.upload(skillMdKey, new ByteArrayInputStream(contentBytes), contentBytes.length, "text/markdown");

        // 3. 保存到数据库
        SkillConfigEntity entity = new SkillConfigEntity();
        entity.setId(id);
        entity.setContent(request.content());
        skillConfigInfra.updateSkillConfig(entity);

        return Result.ok();
    }
}
