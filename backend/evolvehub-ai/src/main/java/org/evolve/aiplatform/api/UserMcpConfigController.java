package org.evolve.aiplatform.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.aiplatform.request.CreateUserMcpConfigRequest;
import org.evolve.aiplatform.request.UpdateUserMcpConfigRequest;
import org.evolve.aiplatform.response.CreateUserMcpConfigResponse;
import org.evolve.aiplatform.response.UpdateUserMcpConfigResponse;
import org.evolve.aiplatform.service.CreateUserMcpConfigManager;
import org.evolve.aiplatform.service.DeleteUserMcpConfigManager;
import org.evolve.aiplatform.service.ListUserMcpConfigManager;
import org.evolve.aiplatform.service.UpdateUserMcpConfigManager;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 用户级 MCP 配置控制器
 * <p>
 * 普通用户操作自己的 MCP 配置（scope=USER）。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/user/mcp-config")
public class UserMcpConfigController {

    @Resource
    private CreateUserMcpConfigManager createUserMcpConfigManager;

    @Resource
    private UpdateUserMcpConfigManager updateUserMcpConfigManager;

    @Resource
    private DeleteUserMcpConfigManager deleteUserMcpConfigManager;

    @Resource
    private ListUserMcpConfigManager listUserMcpConfigManager;

    /** 创建用户级 MCP 配置 */
    @PostMapping("/create")
    public Result<CreateUserMcpConfigResponse> create(@RequestBody @Valid CreateUserMcpConfigRequest request) {
        return Result.ok(createUserMcpConfigManager.execute(request));
    }

    /** 更新用户级 MCP 配置 */
    @PutMapping("/update")
    public Result<UpdateUserMcpConfigResponse> update(@RequestBody @Valid UpdateUserMcpConfigRequest request) {
        return Result.ok(updateUserMcpConfigManager.execute(request));
    }

    /** 删除用户级 MCP 配置 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteUserMcpConfigManager.execute(id);
        return Result.ok();
    }

    /** 分页查询当前用户的 MCP 配置列表 */
    @GetMapping("/list")
    public Result<PageResponse<McpConfigEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listUserMcpConfigManager.execute(new PageRequest(pageNum, pageSize)));
    }
}
