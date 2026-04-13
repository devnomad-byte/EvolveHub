package org.evolve.aiplatform.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.aiplatform.request.CreateUserSkillConfigRequest;
import org.evolve.aiplatform.request.UpdateUserSkillConfigRequest;
import org.evolve.aiplatform.response.CreateUserSkillConfigResponse;
import org.evolve.aiplatform.response.UpdateUserSkillConfigResponse;
import org.evolve.aiplatform.service.CreateUserSkillConfigManager;
import org.evolve.aiplatform.service.DeleteUserSkillConfigManager;
import org.evolve.aiplatform.service.ListUserSkillConfigManager;
import org.evolve.aiplatform.service.UpdateUserSkillConfigManager;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 用户级技能配置控制器
 * <p>
 * 普通用户操作自己的技能配置（scope=USER）。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/user/skill-config")
public class UserSkillConfigController {

    @Resource
    private CreateUserSkillConfigManager createUserSkillConfigManager;

    @Resource
    private UpdateUserSkillConfigManager updateUserSkillConfigManager;

    @Resource
    private DeleteUserSkillConfigManager deleteUserSkillConfigManager;

    @Resource
    private ListUserSkillConfigManager listUserSkillConfigManager;

    /** 创建用户级技能配置 */
    @PostMapping("/create")
    public Result<CreateUserSkillConfigResponse> create(@RequestBody @Valid CreateUserSkillConfigRequest request) {
        return Result.ok(createUserSkillConfigManager.execute(request));
    }

    /** 更新用户级技能配置 */
    @PutMapping("/update")
    public Result<UpdateUserSkillConfigResponse> update(@RequestBody @Valid UpdateUserSkillConfigRequest request) {
        return Result.ok(updateUserSkillConfigManager.execute(request));
    }

    /** 删除用户级技能配置 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteUserSkillConfigManager.execute(id);
        return Result.ok();
    }

    /** 分页查询当前用户的技能配置列表 */
    @GetMapping("/list")
    public Result<PageResponse<SkillConfigEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listUserSkillConfigManager.execute(new PageRequest(pageNum, pageSize)));
    }
}
