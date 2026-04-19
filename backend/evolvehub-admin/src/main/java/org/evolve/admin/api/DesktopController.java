package org.evolve.admin.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.evolve.common.web.response.Result;
import org.evolve.admin.service.*;
import org.evolve.admin.request.CreateCategoryRequest;
import org.evolve.admin.request.UpdateCategoryRequest;
import org.evolve.admin.request.UpdateIconRequest;
import org.evolve.admin.response.CategoryResponse;
import org.evolve.admin.response.IconResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/desktop")
public class DesktopController {

    private final DesktopCategoryListManager categoryListManager;
    private final DesktopCategoryCreateManager categoryCreateManager;
    private final DesktopCategoryUpdateManager categoryUpdateManager;
    private final DesktopCategoryDeleteManager categoryDeleteManager;
    private final DesktopIconListManager iconListManager;
    private final DesktopIconUpdateManager iconUpdateManager;
    private final DesktopAllManager allManager;

    public DesktopController(
            DesktopCategoryListManager categoryListManager,
            DesktopCategoryCreateManager categoryCreateManager,
            DesktopCategoryUpdateManager categoryUpdateManager,
            DesktopCategoryDeleteManager categoryDeleteManager,
            DesktopIconListManager iconListManager,
            DesktopIconUpdateManager iconUpdateManager,
            DesktopAllManager allManager
    ) {
        this.categoryListManager = categoryListManager;
        this.categoryCreateManager = categoryCreateManager;
        this.categoryUpdateManager = categoryUpdateManager;
        this.categoryDeleteManager = categoryDeleteManager;
        this.iconListManager = iconListManager;
        this.iconUpdateManager = iconUpdateManager;
        this.allManager = allManager;
    }

    /** 获取分类列表 */
    @GetMapping("/categories")
    public Result<List<CategoryResponse>> listCategories() {
        return Result.ok(categoryListManager.execute(null));
    }

    /** 创建分类 */
    @PostMapping("/categories")
    public Result<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest req) {
        return Result.ok(categoryCreateManager.execute(req));
    }

    /** 更新分类 */
    @PutMapping("/categories/{id}")
    public Result<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest req) {
        return Result.ok(categoryUpdateManager.execute(req));
    }

    /** 删除分类 */
    @DeleteMapping("/categories/{id}")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        return Result.ok(categoryDeleteManager.execute(id));
    }

    /** 获取图标配置列表 */
    @GetMapping("/icons")
    public Result<?> listIcons() {
        return Result.ok(iconListManager.execute(null));
    }

    /** 更新图标配置 */
    @PutMapping("/icons/{permId}")
    public Result<IconResponse> updateIcon(@PathVariable Long permId, @Valid @RequestBody UpdateIconRequest req) {
        return Result.ok(iconUpdateManager.execute(req));
    }

    /** 获取完整桌面配置 */
    @GetMapping("/all")
    public Result<?> getAll() {
        return Result.ok(allManager.execute(null));
    }
}
