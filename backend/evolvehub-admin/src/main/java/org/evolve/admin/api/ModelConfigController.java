package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.TestConnectionRequest;
import org.evolve.admin.response.BatchTestModelConfigResponse;
import org.evolve.domain.resource.ai.ModelConnectivityTester;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.admin.request.CreateModelConfigRequest;
import org.evolve.admin.request.UpdateModelConfigRequest;
import org.evolve.admin.response.CreateModelConfigResponse;
import org.evolve.admin.response.ModelConfigWithOwnerResponse;
import org.evolve.admin.response.UpdateModelConfigResponse;
import org.evolve.admin.service.*;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 模型配置管理控制器
 * <p>
 * 提供模型配置的增删改查接口。
 * 所有业务逻辑均委托给对应的 Manager 处理，Controller 仅负责接收请求和返回响应。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/model-config")
public class ModelConfigController {

    /** 创建模型配置业务处理器 */
    @Resource
    private CreateModelConfigManager createModelConfigManager;

    /** 更新模型配置业务处理器 */
    @Resource
    private UpdateModelConfigManager updateModelConfigManager;

    /** 查询单个模型配置业务处理器 */
    @Resource
    private GetModelConfigManager getModelConfigManager;

    /** 分页查询模型配置列表业务处理器 */
    @Resource
    private ListModelConfigManager listModelConfigManager;

    /** 删除模型配置业务处理器 */
    @Resource
    private DeleteModelConfigManager deleteModelConfigManager;

    /** SUPER_ADMIN 查看全部模型（含所有者信息） */
    @Resource
    private ListModelConfigAdminManager listModelConfigAdminManager;

    /** 模型连通性测试工具 */
    @Resource
    private ModelConnectivityTester modelConnectivityTester;

    /** 批量测试 SYSTEM 模型连通性 */
    @Resource
    private BatchTestModelConfigManager batchTestModelConfigManager;

    /**
     * 创建模型配置
     * <p>仅允许超级管理员操作，包含模型名称唯一性校验等业务逻辑。</p>
     *
     * @param request 创建模型配置请求体
     * @return 创建成功的模型配置ID
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/create")
    public Result<CreateModelConfigResponse> create(@RequestBody @Valid CreateModelConfigRequest request) {
        return Result.ok(createModelConfigManager.execute(request));
    }

    /**
     * 根据ID查询模型配置详情
     *
     * @param id 模型配置ID
     * @return 模型配置实体信息
     */
    @GetMapping("/{id}")
    public Result<ModelConfigEntity> getById(@PathVariable Long id) {
        return Result.ok(getModelConfigManager.execute(id));
    }

    /**
     * 分页查询模型配置列表
     *
     * @param pageNum  页码，默认为1
     * @param pageSize 每页条数，默认为10
     * @return 分页模型配置列表
     */
    @GetMapping("/list")
    public Result<PageResponse<ModelConfigEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listModelConfigManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * SUPER_ADMIN 查看全部模型配置（带所有者信息）
     * <p>
     * 按用户分组，展示每个用户的个人模型，方便管理员管理所有用户的模型配置。
     * </p>
     */
    @SaCheckRole("SUPER_ADMIN")
    @GetMapping("/admin-all")
    public Result<PageResponse<ModelConfigWithOwnerResponse>> adminAll(@RequestParam(defaultValue = "1") int pageNum,
                                                                      @RequestParam(defaultValue = "100") int pageSize) {
        return Result.ok(listModelConfigAdminManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新模型配置
     * <p>仅允许超级管理员操作，包含模型名称排他唯一性校验等业务逻辑。</p>
     *
     * @param request 更新模型配置请求体
     * @return 更新结果
     */
    @SaCheckRole("SUPER_ADMIN")
    @PutMapping("/update")
    public Result<UpdateModelConfigResponse> update(@RequestBody @Valid UpdateModelConfigRequest request) {
        return Result.ok(updateModelConfigManager.execute(request));
    }

    /**
     * 删除模型配置
     * <p>仅允许超级管理员操作。</p>
     *
     * @param id 模型配置ID
     * @return 删除结果
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteModelConfigManager.execute(id);
        return Result.ok();
    }

    /**
     * 测试模型连通性
     * <p>直接用表单参数测试模型API是否可达，创建前和创建后均可调用。</p>
     *
     * @param request 包含 provider、baseUrl、apiKey
     * @return 测试结果
     */
    @PostMapping("/test-connection")
    public Result<String> testConnection(@RequestBody @Valid TestConnectionRequest request) {
        ModelConnectivityTester.TestResult result = modelConnectivityTester.test(
                request.getProvider(), request.getBaseUrl(), request.getApiKey());
        if (result.success()) {
            return Result.ok("连通性测试通过");
        }
        return Result.fail("连通性测试失败: " + result.message());
    }

    /**
     * 批量测试 SYSTEM 公共模型连通性
     * <p>
     * 查询所有 scope=SYSTEM 的模型，逐个测试连通性，
     * 失败模型自动禁用（enabled=0），返回每个模型的测试结果。
     * </p>
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/batch-test")
    public Result<BatchTestModelConfigResponse> batchTest() {
        return Result.ok(batchTestModelConfigManager.execute());
    }
}
