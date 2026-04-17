package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateMcpToolRequest;
import org.evolve.admin.request.UpdateMcpToolRequest;
import org.evolve.admin.request.DeleteMcpToolRequest;
import org.evolve.admin.request.CreateMcpToolGrantRequest;
import org.evolve.admin.response.McpToolResponse;
import org.evolve.admin.response.CreateMcpToolResponse;
import org.evolve.admin.response.UpdateMcpToolResponse;
import org.evolve.admin.response.CreateMcpToolGrantResponse;
import org.evolve.admin.service.*;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * MCP 工具管理控制器
 *
 * @author qjc
 */
@RestController
@RequestMapping("/mcp-tool")
public class McpToolController {

    @Resource
    private CreateMcpToolManager createMcpToolManager;

    @Resource
    private UpdateMcpToolManager updateMcpToolManager;

    @Resource
    private GetMcpToolManager getMcpToolManager;

    @Resource
    private ListMcpToolManager listMcpToolManager;

    @Resource
    private DeleteMcpToolManager deleteMcpToolManager;

    @Resource
    private CreateMcpToolGrantManager createMcpToolGrantManager;

    @Resource
    private DeleteMcpToolGrantManager deleteMcpToolGrantManager;

    /**
     * 创建 MCP 工具
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/create")
    public Result<CreateMcpToolResponse> create(@RequestBody @Valid CreateMcpToolRequest request) {
        return Result.ok(createMcpToolManager.execute(request));
    }

    /**
     * 获取 MCP 工具详情
     */
    @GetMapping("/{id}")
    public Result<McpToolResponse> getById(@PathVariable Long id) {
        return Result.ok(getMcpToolManager.execute(id));
    }

    /**
     * 分页查询 MCP 工具列表
     */
    @GetMapping("/list")
    public Result<PageResponse<McpToolResponse>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listMcpToolManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新 MCP 工具
     */
    @SaCheckRole("SUPER_ADMIN")
    @PutMapping("/update")
    public Result<UpdateMcpToolResponse> update(@RequestBody @Valid UpdateMcpToolRequest request) {
        return Result.ok(updateMcpToolManager.execute(request));
    }

    /**
     * 删除 MCP 工具
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteMcpToolManager.execute(new DeleteMcpToolRequest(id));
        return Result.ok();
    }

    /**
     * 授权 MCP 工具给用户/部门/角色
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/grant")
    public Result<CreateMcpToolGrantResponse> grant(@RequestBody @Valid CreateMcpToolGrantRequest request) {
        return Result.ok(createMcpToolGrantManager.execute(request));
    }

    /**
     * 撤销 MCP 工具授权
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/grant/{id}")
    public Result<Void> revokeGrant(@PathVariable Long id) {
        deleteMcpToolGrantManager.execute(id);
        return Result.ok();
    }
}
