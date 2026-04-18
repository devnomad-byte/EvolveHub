package org.evolve.admin.hub.adapter;

import org.evolve.admin.hub.SkillHubAdapter;
import org.evolve.admin.hub.model.HubSearchResult;
import org.evolve.admin.hub.model.HubSkillBundle;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ClawHubAdapter implements SkillHubAdapter {

    private static final String HUB_NAME = "ClawHub";
    private static final String API_BASE = "https://api.clawhub.ai/v1";

    private final RestTemplate restTemplate;

    public ClawHubAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String getHubName() {
        return HUB_NAME;
    }

    @Override
    public List<HubSearchResult> search(String keyword, int page, int pageSize) {
        try {
            String url = API_BASE + "/skills/search?q=" + keyword + "&page=" + page + "&size=" + pageSize;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            List<HubSearchResult> results = new ArrayList<>();
            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("data");
                for (Map<String, Object> item : items) {
                    HubSearchResult result = new HubSearchResult();
                    result.setName((String) item.get("name"));
                    result.setDescription((String) item.get("description"));
                    result.setAuthor((String) item.get("author"));
                    result.setHubName(HUB_NAME);
                    result.setBundleUrl((String) item.get("bundle_url"));
                    result.setTags((List<String>) item.get("tags"));
                    result.setDownloads((Integer) item.get("downloads"));
                    result.setVersion((String) item.get("version"));
                    results.add(result);
                }
            }
            return results;
        } catch (Exception e) {
            // 返回空列表，搜索失败不影响其他 Hub
            return new ArrayList<>();
        }
    }

    @Override
    public HubSkillBundle getBundleInfo(String bundleUrl) {
        // 获取技能包元信息，不下载内容
        try {
            // 实际实现调用 GET /skills/{name} 获取元信息
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public HubSkillBundle downloadBundle(String bundleUrl) {
        try {
            byte[] zipData = restTemplate.getForObject(bundleUrl, byte[].class);

            HubSkillBundle bundle = new HubSkillBundle();
            bundle.setSourceUrl(bundleUrl);
            bundle.setZipData(zipData);

            // 解析 zip 包，提取所有文件
            Map<String, byte[]> files = new HashMap<>();
            String content = null;
            String readme = null;

            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
                ZipEntry entry;
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

                    // 保留相对路径
                    files.put(entry.getName(), fileContent);
                    zis.closeEntry();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse zip: " + e.getMessage());
            }

            bundle.setContent(content);
            bundle.setReadme(readme);
            bundle.setFiles(files);

            return bundle;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from ClawHub: " + e.getMessage());
        }
    }
}