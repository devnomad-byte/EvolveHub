package org.evolve.admin.hub.adapter;

import lombok.extern.slf4j.Slf4j;
import org.evolve.admin.hub.SkillHubAdapter;
import org.evolve.admin.hub.model.HubSearchResult;
import org.evolve.admin.hub.model.HubSkillBundle;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class ModelScopeAdapter implements SkillHubAdapter {

    private static final String HUB_NAME = "ModelScope";
    private static final String API_BASE = "https://www.modelscope.cn/api/v1";
    // Skill detail URL format: https://modelscope.cn/api/v1/skills/@{owner}/{skill_name}
    // Search page URL format: https://modelscope.cn/skills/@{owner}/{skill_name}

    private final RestTemplate restTemplate;

    public ModelScopeAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);   // 10s connection timeout
        factory.setReadTimeout(60000);       // 60s read timeout
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String getHubName() {
        return HUB_NAME;
    }

    @Override
    public List<HubSearchResult> search(String keyword, int page, int pageSize) {
        // ModelScope 的搜索需要使用网页端 API
        // 暂时返回空列表，实际搜索通过前端过滤或单独实现
        return new ArrayList<>();
    }

    @Override
    public HubSkillBundle getBundleInfo(String bundleUrl) {
        // 获取技能包元信息，不下载内容
        return null;
    }

    /**
     * 从 ModelScope URL 解析出 owner 和 skill_name
     * URL 格式: https://modelscope.cn/skills/@owner/skill-name
     * 或: https://www.modelscope.cn/skills/@owner/skill-name
     */
    private String[] parseModelScopeUrl(String bundleUrl) {
        try {
            URI uri = URI.create(bundleUrl);
            String path = uri.getPath();
            if (path == null) return null;

            // 路径格式: /skills/@owner/skill-name
            String[] parts = path.split("/");
            if (parts.length < 3) return null;

            int skillsIdx = -1;
            for (int i = 0; i < parts.length; i++) {
                if ("skills".equals(parts[i])) {
                    skillsIdx = i;
                    break;
                }
            }
            if (skillsIdx < 0 || skillsIdx + 2 >= parts.length) return null;

            String ownerPart = parts[skillsIdx + 1]; // @owner
            String skillName = parts[skillsIdx + 2];

            String owner = ownerPart.startsWith("@") ? ownerPart.substring(1) : ownerPart;
            return new String[]{owner, skillName};
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public HubSkillBundle downloadBundle(String bundleUrl) {
        // 清理 URL（去除首尾空格）
        if (bundleUrl != null) {
            bundleUrl = bundleUrl.trim();
        }
        // bundleUrl 格式: https://modelscope.cn/skills/@owner/skill-name
        // 1. 解析 URL 获取 owner 和 skill_name
        // 2. 调用 GET /api/v1/skills/@{owner}/{skill_name} 获取详情
        // 3. 从详情中获取 SourceURL（GitHub 地址）
        // 4. 从 GitHub 下载实际内容

        String[] parsed = parseModelScopeUrl(bundleUrl);
        if (parsed == null) {
            throw new RuntimeException("Invalid ModelScope URL format: " + bundleUrl);
        }
        String owner = parsed[0];
        String skillName = parsed[1];

        // 调用 ModelScope API 获取详情
        String detailUrl = API_BASE + "/skills/@" + owner + "/" + skillName;
        log.info("[ModelScope] 开始获取技能详情: {}", detailUrl);
        Map<String, Object> detail = restTemplate.getForObject(detailUrl, Map.class);
        log.info("[ModelScope] 获取详情完成: {}", detail != null ? "有数据" : "null");

        if (detail == null || !"200".equals(String.valueOf(detail.get("Code")))) {
            throw new RuntimeException("Failed to get skill detail from ModelScope");
        }

        Map<String, Object> data = (Map<String, Object>) detail.get("Data");
        if (data == null) {
            throw new RuntimeException("Invalid ModelScope response: missing Data");
        }

        // 获取源地址（GitHub 等）
        String sourceUrl = (String) data.get("SourceURL");
        String readmeContent = (String) data.get("ReadMeContent");
        String skillDisplayName = (String) data.get("Name");
        if (skillDisplayName == null) {
            skillDisplayName = skillName;
        }

        HubSkillBundle bundle = new HubSkillBundle();
        bundle.setSourceUrl(bundleUrl);
        bundle.setName(skillDisplayName);

        // 如果有 GitHub 源地址，从 GitHub 下载
        if (sourceUrl != null && sourceUrl.contains("github.com")) {
            try {
                String githubZipUrl = convertGitHubToArchiveUrl(sourceUrl);
                log.info("[ModelScope] GitHub zip URL: {}", githubZipUrl);
                byte[] zipData = restTemplate.getForObject(githubZipUrl, byte[].class);
                log.info("[ModelScope] GitHub zip 下载完成，大小: {} bytes", zipData != null ? zipData.length : 0);
                if (zipData != null) {
                    return parseZipBundle(bundle, zipData, skillDisplayName);
                }
            } catch (Exception e) {
                log.warn("[ModelScope] GitHub 下载失败: {}", e.getMessage());
                // GitHub 下载失败，尝试使用 ReadMeContent
            }
        }

        // 如果没有 GitHub 源或下载失败，使用 ReadMeContent 作为 SKILL.md
        if (readmeContent != null && !readmeContent.isEmpty()) {
            log.info("[ModelScope] 使用 ReadMeContent 作为 SKILL.md，长度: {}", readmeContent.length());
            Map<String, byte[]> files = new HashMap<>();
            files.put("SKILL.md", readmeContent.getBytes(StandardCharsets.UTF_8));
            bundle.setFiles(files);
            bundle.setContent(readmeContent);
        } else {
            throw new RuntimeException("ModelScope skill has no downloadable source and no ReadMeContent");
        }

        return bundle;
    }

    private HubSkillBundle parseZipBundle(HubSkillBundle bundle, byte[] zipData, String skillName) {
        Map<String, byte[]> files = new HashMap<>();
        String content = null;
        String readme = null;

        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(zipData))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }
                String filename = entry.getName().toUpperCase();
                byte[] fileContent = zis.readAllBytes();

                if (filename.endsWith("SKILL.MD") || filename.endsWith("SKILL.MARKDOWN")) {
                    content = new String(fileContent, StandardCharsets.UTF_8);
                } else if (filename.endsWith("README.MD") || filename.endsWith("README.TXT")) {
                    readme = new String(fileContent, StandardCharsets.UTF_8);
                }

                files.put(entry.getName(), fileContent);
                zis.closeEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse zip bundle: " + e.getMessage());
        }

        bundle.setContent(content);
        bundle.setReadme(readme);
        bundle.setFiles(files);
        return bundle;
    }

    /**
     * 将 GitHub tree URL 转换为 archive zip URL
     * 例如: https://github.com/owner/repo/tree/branch/path
     * 转换为: https://github.com/owner/repo/archive/refs/heads/branch.zip
     */
    private String convertGitHubToArchiveUrl(String gitHubUrl) {
        try {
            String url = gitHubUrl;
            if (url.contains("/tree/")) {
                int treeIdx = url.indexOf("/tree/");
                String beforeTree = url.substring(0, treeIdx);
                String afterTree = url.substring(treeIdx + 6);
                int slashIdx = afterTree.indexOf("/");
                String branch = slashIdx > 0 ? afterTree.substring(0, slashIdx) : afterTree;
                return beforeTree + "/archive/refs/heads/" + branch + ".zip";
            }
            // 如果是原始的 github.com/owner/repo 格式，返回默认 main 分支
            if (url.matches("https://github\\.com/[^/]+/[^/]+/?$")) {
                return url + "/archive/refs/heads/main.zip";
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
