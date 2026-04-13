package org.evolve.aiplatform.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.aiplatform.request.CreateUserModelConfigRequest;
import org.evolve.aiplatform.request.UpdateUserModelConfigRequest;
import org.evolve.aiplatform.response.CreateUserModelConfigResponse;
import org.evolve.aiplatform.response.UpdateUserModelConfigResponse;
import org.evolve.aiplatform.service.CreateUserModelConfigManager;
import org.evolve.aiplatform.service.DeleteUserModelConfigManager;
import org.evolve.aiplatform.service.ListUserModelConfigManager;
import org.evolve.aiplatform.service.UpdateUserModelConfigManager;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 用户级模型配置控制器
 * <p>
 * 普通用户操作自己的模型配置（scope=USER）。
 * 所有接口均登录即可访问，业务层校验 owner_id。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/user/model-config")
public class UserModelConfigController {

    @Resource
    private CreateUserModelConfigManager createUserModelConfigManager;

    @Resource
    private UpdateUserModelConfigManager updateUserModelConfigManager;

    @Resource
    private DeleteUserModelConfigManager deleteUserModelConfigManager;

    @Resource
    private ListUserModelConfigManager listUserModelConfigManager;

    /**
     * 创建用户级模型配置
     *
     * @param request 创建请求
     * @return 新创建的模型配置 ID
     */
    @PostMapping("/create")
    public Result<CreateUserModelConfigResponse> create(@RequestBody @Valid CreateUserModelConfigRequest request) {
        return Result.ok(createUserModelConfigManager.execute(request));
    }

    /**
     * 更新用户级模型配置
     *
     * @param request 更新请求
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<UpdateUserModelConfigResponse> update(@RequestBody @Valid UpdateUserModelConfigRequest request) {
        return Result.ok(updateUserModelConfigManager.execute(request));
    }

    /**
     * 删除用户级模型配置
     *
     * @param id 模型配置 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteUserModelConfigManager.execute(id);
        return Result.ok();
    }

    /**
     * 分页查询当前用户的模型配置列表
     *
     * @param pageNum  页码，默认为 1
     * @param pageSize 每页条数，默认为 10
     * @return 分页模型配置列表
     */
    @GetMapping("/list")
    public Result<PageResponse<ModelConfigEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listUserModelConfigManager.execute(new PageRequest(pageNum, pageSize)));
    }
}
