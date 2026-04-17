package org.evolve.admin.hub;

import org.evolve.admin.hub.model.HubSearchResult;
import org.evolve.admin.hub.model.HubSkillBundle;
import java.util.List;

public interface SkillHubAdapter {

    /**
     * 获取 Hub 名称
     */
    String getHubName();

    /**
     * 搜索技能
     */
    List<HubSearchResult> search(String keyword, int page, int pageSize);

    /**
     * 获取技能包信息（不下载内容）
     */
    HubSkillBundle getBundleInfo(String bundleUrl);

    /**
     * 下载并解析技能包
     */
    HubSkillBundle downloadBundle(String bundleUrl);

    /**
     * 获取支持的搜索参数
     */
    default List<String> getSupportedTags() {
        return List.of();
    }
}
