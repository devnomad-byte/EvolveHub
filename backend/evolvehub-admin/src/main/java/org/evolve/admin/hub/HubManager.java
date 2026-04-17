package org.evolve.admin.hub;

import jakarta.annotation.Resource;
import org.evolve.admin.hub.model.HubSearchResult;
import org.evolve.admin.hub.model.HubSkillBundle;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class HubManager {

    @Resource
    private List<SkillHubAdapter> adapters;

    /**
     * 获取所有支持的 Hub
     */
    public List<String> getSupportedHubs() {
        return adapters.stream()
            .map(SkillHubAdapter::getHubName)
            .collect(Collectors.toList());
    }

    /**
     * 并行搜索所有 Hub
     */
    public List<HubSearchResult> searchAll(String keyword, int page, int pageSize) {
        List<CompletableFuture<List<HubSearchResult>>> futures = adapters.stream()
            .map(adapter -> CompletableFuture.supplyAsync(() ->
                adapter.search(keyword, page, pageSize)
            ))
            .collect(Collectors.toList());

        List<HubSearchResult> results = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .sorted(Comparator.comparingInt(HubSearchResult::getDownloads).reversed())
            .collect(Collectors.toList());

        return results;
    }

    /**
     * 搜索指定 Hub
     */
    public List<HubSearchResult> search(String hubName, String keyword, int page, int pageSize) {
        return adapters.stream()
            .filter(a -> a.getHubName().equalsIgnoreCase(hubName))
            .findFirst()
            .map(adapter -> adapter.search(keyword, page, pageSize))
            .orElse(new ArrayList<>());
    }

    /**
     * 下载技能包
     */
    public HubSkillBundle downloadBundle(String hubName, String bundleUrl) {
        return adapters.stream()
            .filter(a -> a.getHubName().equalsIgnoreCase(hubName))
            .findFirst()
            .map(adapter -> adapter.downloadBundle(bundleUrl))
            .orElseThrow(() -> new RuntimeException("Hub not supported: " + hubName));
    }
}
