package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateSkillConfigRequest;
import org.evolve.admin.request.UpdateSkillConfigRequest;
import org.evolve.admin.response.CreateSkillConfigResponse;
import org.evolve.admin.response.UpdateSkillConfigResponse;
import org.evolve.admin.service.*;
import org.evolve.common.model.SkillConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 系统级技能配置管理控制器
 * <p>
 * 提供系统级技能配置的增删改查接口，scope 固定为 SYSTEM。
 * </p>
 *
 * @author zhao
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

    /**
     * 创建系统级技能配置
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
     * 分页查询系统级技能配置列表
     */
    @GetMapping("/list")
    public Result<PageResponse<SkillConfigEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listSkillConfigManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新系统级技能配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PutMapping("/update")
    public Result<UpdateSkillConfigResponse> update(@RequestBody @Valid UpdateSkillConfigRequest request) {
        return Result.ok(updateSkillConfigManager.execute(request));
    }

    /**
     * 删除系统级技能配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteSkillConfigManager.execute(id);
        return Result.ok();
    }
}
