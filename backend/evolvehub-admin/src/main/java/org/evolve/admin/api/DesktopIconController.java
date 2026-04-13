package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateDesktopIconRequest;
import org.evolve.admin.request.UpdateDesktopIconRequest;
import org.evolve.admin.response.DesktopIconResponse;
import org.evolve.admin.service.*;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 桌面图标管理控制器
 *
 * @author zhao
 */
@RestController
@RequestMapping("/desktop-icon")
public class DesktopIconController {

    @Resource
    private CreateDesktopIconManager createDesktopIconManager;

    @Resource
    private UpdateDesktopIconManager updateDesktopIconManager;

    @Resource
    private GetDesktopIconManager getDesktopIconManager;

    @Resource
    private ListDesktopIconManager listDesktopIconManager;

    @Resource
    private DeleteDesktopIconManager deleteDesktopIconManager;

    /**
     * 查询所有桌面图标
     */
    @SaCheckRole("SUPER_ADMIN")
    @GetMapping("/list")
    public Result<List<DesktopIconResponse>> list() {
        return Result.ok(listDesktopIconManager.execute());
    }

    /**
     * 查询单个桌面图标
     */
    @SaCheckRole("SUPER_ADMIN")
    @GetMapping("/{id}")
    public Result<DesktopIconResponse> getById(@PathVariable Long id) {
        return Result.ok(getDesktopIconManager.execute(id));
    }

    /**
     * 创建桌面图标
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping
    public Result<DesktopIconResponse> create(@RequestBody @Valid CreateDesktopIconRequest request) {
        return Result.ok(createDesktopIconManager.execute(request));
    }

    /**
     * 更新桌面图标
     */
    @SaCheckRole("SUPER_ADMIN")
    @PutMapping("/{id}")
    public Result<DesktopIconResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateDesktopIconRequest request) {
        // 将 path 中的 id 设置到 request 中
        UpdateDesktopIconRequest requestWithId = new UpdateDesktopIconRequest(
                id,
                request.permName(),
                request.icon(),
                request.gradient(),
                request.path(),
                request.sort(),
                request.defaultWidth(),
                request.defaultHeight(),
                request.minWidth(),
                request.minHeight(),
                request.dockOrder(),
                request.status()
        );
        return Result.ok(updateDesktopIconManager.execute(requestWithId));
    }

    /**
     * 删除桌面图标
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteDesktopIconManager.execute(id);
        return Result.ok();
    }
}
