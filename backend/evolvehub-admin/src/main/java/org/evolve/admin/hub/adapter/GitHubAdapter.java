package org.evolve.admin.hub.adapter;

import lombok.extern.slf4j.Slf4j;
import org.evolve.admin.hub.SkillHubAdapter;
import org.evolve.admin.hub.model.HubSearchResult;
import org.evolve.admin.hub.model.HubSkillBundle;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
public class GitHubAdapter implements SkillHubAdapter {

    private static final String HUB_NAME = "GitHub";
    private static final String GITHUB_API = "https://api.github.com";

    private final RestTemplate restTemplate;

    public GitHubAdapter() {
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
        // GitHub 不支持搜索，暂时返回空列表
        return new ArrayList<>();
    }

    @Override
    public HubSkillBundle getBundleInfo(String bundleUrl) {
        return null;
    }

    @Override
    public HubSkillBundle downloadBundle(String bundleUrl) {
        // 解析 GitHub URL
        ParsedGitHubUrl parsed = parseGitHubUrl(bundleUrl);
        if (parsed == null) {
            throw new RuntimeException("Invalid GitHub URL format: " + bundleUrl);
        }

        log.info("[GitHub] 下载技能: owner={}, repo={}, branch={}, path={}",
                parsed.owner, parsed.repo, parsed.branch, parsed.path);

        // 构建 archive URL 并下载 zip
        String archiveUrl = String.format("https://github.com/%s/%s/archive/%s.zip",
                parsed.owner, parsed.repo, parsed.branch);
        log.info("[GitHub] Archive URL: {}", archiveUrl);

        byte[] zipData;
        try {
            zipData = restTemplate.getForObject(archiveUrl, byte[].class);
        } catch (Exception e) {
            throw new RuntimeException("从 GitHub 下载失败: " + e.getMessage());
        }

        if (zipData == null || zipData.length == 0) {
            throw new RuntimeException("GitHub 返回空数据");
        }

        // 解析 zip 并过滤指定目录
        return parseAndFilterZip(zipData, parsed.path, parsed.repo, parsed.owner);
    }

    /**
     * 解析 GitHub URL
     * 支持格式：
     * - https://github.com/owner/repo/tree/branch/path
     * - https://github.com/owner/repo
     * - https://github.com/owner/repo/tree/branch (root)
     */
    private ParsedGitHubUrl parseGitHubUrl(String url) {
        try {
            // 匹配: https://github.com/{owner}/{repo}/tree/{branch}/{path}
            Pattern treePattern = Pattern.compile(
                    "github\\.com/([^/]+)/([^/]+)/tree/([^/]+)/(.+)");
            Matcher m = treePattern.matcher(url);
            if (m.find()) {
                return new ParsedGitHubUrl(m.group(1), m.group(2), m.group(3), m.group(4));
            }

            // 匹配: https://github.com/{owner}/{repo}/tree/{branch}
            Pattern branchPattern = Pattern.compile(
                    "github\\.com/([^/]+)/([^/]+)/tree/([^/]+)/?$");
            m = branchPattern.matcher(url);
            if (m.find()) {
                return new ParsedGitHubUrl(m.group(1), m.group(2), m.group(3), "");
            }

            // 匹配: https://github.com/{owner}/{repo}
            Pattern repoPattern = Pattern.compile(
                    "github\\.com/([^/]+)/([^/]+)/?$");
            m = repoPattern.matcher(url);
            if (m.find()) {
                return new ParsedGitHubUrl(m.group(1), m.group(2), "main", "");
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 zip 并过滤指定目录
     */
    private HubSkillBundle parseAndFilterZip(byte[] zipData, String filterPath,
                                            String repoName, String owner) {
        Map<String, byte[]> allFiles = new LinkedHashMap<>();
        String skillName = filterPath.isEmpty() ? repoName : filterPath.split("/")[0];
        String mainContent = null;
        String readme = null;

        // zip 内部路径格式: {repo}-{branch}/path/to/file
        String zipPrefix = repoName + "-"; // 默认前缀（下载的 zip 格式）

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                String entryName = entry.getName();

                // zip 内部有前缀：repo-branch/
                if (!entryName.startsWith(zipPrefix)) {
                    // 尝试去掉第一层目录
                    int slashIdx = entryName.indexOf('/');
                    if (slashIdx >= 0) {
                        entryName = entryName.substring(slashIdx + 1);
                    }
                } else {
                    entryName = entryName.substring(zipPrefix.length());
                }

                // 如果指定了 filterPath，只保留匹配的文件
                if (!filterPath.isEmpty()) {
                    if (!entryName.startsWith(filterPath + "/") &&
                        !entryName.equals(filterPath)) {
                        zis.closeEntry();
                        continue;
                    }
                    // 去掉 filterPath 前缀，得到相对路径
                    entryName = entryName.substring(filterPath.length());
                    if (entryName.startsWith("/")) {
                        entryName = entryName.substring(1);
                    }
                }

                // 跳过空文件名
                if (entryName.isEmpty()) {
                    zis.closeEntry();
                    continue;
                }

                byte[] content = zis.readAllBytes();
                allFiles.put(entryName, content);

                // 记录主内容（SKILL.md）
                String upperName = entryName.toUpperCase();
                if (upperName.endsWith("SKILL.MD") || upperName.endsWith("SKILL.MARKDOWN")) {
                    if (mainContent == null) {
                        mainContent = new String(content, StandardCharsets.UTF_8);
                    }
                } else if (upperName.endsWith("README.MD") && readme == null) {
                    readme = new String(content, StandardCharsets.UTF_8);
                }

                zis.closeEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException("解析 GitHub zip 失败: " + e.getMessage());
        }

        if (allFiles.isEmpty()) {
            throw new RuntimeException("GitHub zip 中没有找到技能文件");
        }

        HubSkillBundle bundle = new HubSkillBundle();
        bundle.setName(skillName);
        bundle.setSourceUrl("https://github.com/" + owner + "/" + repoName);
        bundle.setContent(mainContent);
        bundle.setReadme(readme);
        bundle.setFiles(allFiles);

        log.info("[GitHub] 解析完成，技能名={}, 文件数={}", skillName, allFiles.size());
        return bundle;
    }

    private record ParsedGitHubUrl(String owner, String repo, String branch, String path) {}
}
