package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.AssignResourceGrantRequest;
import org.evolve.admin.request.RevokeResourceGrantRequest;
import org.evolve.admin.response.AssignResourceGrantResponse;
import org.evolve.admin.service.AssignResourceGrantManager;
import org.evolve.admin.service.ListResourceGrantManager;
import org.evolve.admin.service.RevokeResourceGrantManager;
import org.evolve.common.model.ResourceGrantEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 资源授权管理控制器
 * <p>
 * 管理员将系统级资源（模型/Skill/MCP）授权给用户，或撤销授权。
 * 所有接口仅超级管理员可操作。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@RestController
@RequestMapping("/resource-grant")
public class ResourceGrantController {

    @Resource
    private AssignResourceGrantManager assignResourceGrantManager;

    @Resource
    private RevokeResourceGrantManager revokeResourceGrantManager;

    @Resource
    private ListResourceGrantManager listResourceGrantManager;

    /**
     * 将系统资源授权给用户
     *
     * @param request 授权请求（userId + resourceType + resourceId）
     * @return 授权记录 ID
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/assign")
    public Result<AssignResourceGrantResponse> assign(@RequestBody @Valid AssignResourceGrantRequest request) {
        return Result.ok(assignResourceGrantManager.execute(request));
    }

    /**
     * 撤销用户的资源授权
     *
     * @param request 撤销请求（userId + resourceType + resourceId）
     * @return 操作结果
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/revoke")
    public Result<Void> revoke(@RequestBody @Valid RevokeResourceGrantRequest request) {
        revokeResourceGrantManager.execute(request);
        return Result.ok();
    }

    /**
     * 分页查询授权列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页授权列表
     */
    @SaCheckRole("SUPER_ADMIN")
    @GetMapping("/list")
    public Result<PageResponse<ResourceGrantEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listResourceGrantManager.execute(new PageRequest(pageNum, pageSize)));
    }
}
