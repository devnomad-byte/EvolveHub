package org.evolve.aiplatform.api;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryManagedItemVO;
import org.evolve.aiplatform.request.SaveMemoryProfileRequest;
import org.evolve.aiplatform.response.MemoryProfileResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户记忆控制器
 *
 * @author TellyJiang
 * @since 2026-04-17
 */
@RestController
@RequestMapping("/user/memory")
public class UserMemoryController {

    @Resource
    private MemoryApi memoryApi;

    /**
     * 查询用户画像
     *
     * @param targetUserId 目标用户 ID
     * @return 画像 Markdown
     * @author TellyJiang
     * @since 2026-04-17
     */
    @GetMapping("/profile")
    public Result<MemoryProfileResponse> getProfile(@RequestParam(required = false) Long targetUserId) {
        Long resolvedUserId = resolveTargetUserId(targetUserId);
        return Result.ok(new MemoryProfileResponse(
                resolvedUserId,
                memoryApi.getMemoryProfile(resolvedUserId)
        ));
    }

    /**
     * 保存用户画像
     *
     * @param request 保存请求
     * @return 最新画像 Markdown
     * @author TellyJiang
     * @since 2026-04-17
     */
    @PutMapping("/profile")
    public Result<MemoryProfileResponse> saveProfile(@RequestBody SaveMemoryProfileRequest request) {
        Long resolvedUserId = resolveTargetUserId(request.targetUserId());
        String markdownContent = request.markdownContent() == null ? "" : request.markdownContent();
        return Result.ok(new MemoryProfileResponse(
                resolvedUserId,
                memoryApi.saveMemoryProfileMarkdown(resolvedUserId, markdownContent)
        ));
    }

    /**
     * 查询可管理长期记忆
     *
     * @param targetUserId 目标用户 ID
     * @return 长期记忆列表
     * @author TellyJiang
     * @since 2026-04-17
     */
    @GetMapping("/items")
    public Result<List<MemoryManagedItemVO>> listItems(@RequestParam(required = false) Long targetUserId) {
        Long resolvedUserId = resolveTargetUserId(targetUserId);
        return Result.ok(memoryApi.listManagedMemories(resolvedUserId));
    }

    /**
     * 删除长期记忆
     *
     * @param id           记忆主键
     * @param targetUserId 目标用户 ID
     * @return 删除结果
     * @author TellyJiang
     * @since 2026-04-17
     */
    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id, @RequestParam(required = false) Long targetUserId) {
        Long resolvedUserId = resolveTargetUserId(targetUserId);
        memoryApi.deleteManagedMemory(resolvedUserId, id);
        return Result.ok();
    }

    private Long resolveTargetUserId(Long targetUserId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (targetUserId == null || targetUserId.equals(currentUserId)) {
            return currentUserId;
        }
        if (isAdminOperator()) {
            return targetUserId;
        }
        return currentUserId;
    }

    /**
     * 判断当前操作者是否为可跨用户查看记忆的管理员角色
     *
     * @return 是否管理员
     * @author TellyJiang
     * @since 2026-04-18
     */
    private boolean isAdminOperator() {
        return StpUtil.getRoleList().stream()
                .anyMatch(roleCode -> "SUPER_ADMIN".equals(roleCode) || "ADMIN".equals(roleCode));
    }
}
