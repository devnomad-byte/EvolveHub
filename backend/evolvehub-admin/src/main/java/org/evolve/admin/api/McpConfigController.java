package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateMcpConfigRequest;
import org.evolve.admin.request.UpdateMcpConfigRequest;
import org.evolve.admin.response.CreateMcpConfigResponse;
import org.evolve.admin.response.UpdateMcpConfigResponse;
import org.evolve.admin.service.*;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 系统级 MCP 配置管理控制器
 * <p>
 * 提供系统级 MCP 配置的增删改查接口，scope 固定为 SYSTEM。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/mcp-config")
public class McpConfigController {

    @Resource
    private CreateMcpConfigManager createMcpConfigManager;

    @Resource
    private UpdateMcpConfigManager updateMcpConfigManager;

    @Resource
    private GetMcpConfigManager getMcpConfigManager;

    @Resource
    private ListMcpConfigManager listMcpConfigManager;

    @Resource
    private DeleteMcpConfigManager deleteMcpConfigManager;

    /**
     * 创建系统级 MCP 配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/create")
    public Result<CreateMcpConfigResponse> create(@RequestBody @Valid CreateMcpConfigRequest request) {
        return Result.ok(createMcpConfigManager.execute(request));
    }

    /**
     * 根据 ID 查询 MCP 配置详情
     */
    @GetMapping("/{id}")
    public Result<McpConfigEntity> getById(@PathVariable Long id) {
        return Result.ok(getMcpConfigManager.execute(id));
    }

    /**
     * 分页查询系统级 MCP 配置列表
     */
    @GetMapping("/list")
    public Result<PageResponse<McpConfigEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listMcpConfigManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新系统级 MCP 配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PutMapping("/update")
    public Result<UpdateMcpConfigResponse> update(@RequestBody @Valid UpdateMcpConfigRequest request) {
        return Result.ok(updateMcpConfigManager.execute(request));
    }

    /**
     * 删除系统级 MCP 配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteMcpConfigManager.execute(id);
        return Result.ok();
    }
}
