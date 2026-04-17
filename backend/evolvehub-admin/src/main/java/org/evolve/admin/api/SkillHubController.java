package org.evolve.admin.api;

import jakarta.annotation.Resource;
import org.evolve.admin.hub.HubManager;
import org.evolve.admin.hub.model.HubSearchResult;
import org.evolve.admin.hub.model.HubSkillBundle;
import org.evolve.admin.request.InstallSkillFromHubRequest;
import org.evolve.admin.service.SkillInstallService;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skill-config/hub")
public class SkillHubController {

    @Resource
    private HubManager hubManager;

    @Resource
    private SkillInstallService skillInstallService;

    /**
     * 获取支持的 Hub 列表
     */
    @GetMapping("/adapters")
    public Result<List<String>> getSupportedHubs() {
        return Result.ok(hubManager.getSupportedHubs());
    }

    /**
     * 搜索所有 Hub
     */
    @GetMapping("/search")
    public Result<List<HubSearchResult>> search(
            @RequestParam(required = false) String hub,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<HubSearchResult> results;
        if (hub != null && !hub.isEmpty()) {
            results = hubManager.search(hub, keyword, page, pageSize);
        } else {
            results = hubManager.searchAll(keyword, page, pageSize);
        }
        return Result.ok(results);
    }

    /**
     * 获取技能包信息
     */
    @GetMapping("/bundle")
    public Result<HubSkillBundle> getBundle(
            @RequestParam String hub,
            @RequestParam String url) {
        HubSkillBundle bundle = hubManager.downloadBundle(hub, url);
        return Result.ok(bundle);
    }

    /**
     * 从 Hub 安装技能
     */
    @PostMapping("/install")
    public Result<Long> install(@RequestBody InstallSkillFromHubRequest request) {
        Long skillId = skillInstallService.installFromHub(
                request.hubName(), request.bundleUrl(), request.shouldSkipScan());
        return Result.ok(skillId);
    }
}