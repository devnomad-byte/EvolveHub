package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.hub.HubManager;
import org.evolve.admin.hub.model.HubSkillBundle;
import org.evolve.admin.response.SecurityScanResult;
import org.evolve.admin.utils.FileStorageUtil;
import org.evolve.common.web.exception.SecurityScanException;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

@Service
public class SkillInstallService {

    @Resource
    private HubManager hubManager;

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Resource
    private FileStorageUtil fileStorageUtil;

    @Resource
    private SecurityScanner securityScanner;

    @Transactional
    public List<Long> installFromHub(String hubName, String bundleUrl) {
        return installFromHub(hubName, bundleUrl, false);
    }

    @Transactional
    public List<Long> installFromHub(String hubName, String bundleUrl, boolean skipScan) {
        // 1. 下载并解析技能包
        HubSkillBundle bundle = hubManager.downloadBundle(hubName, bundleUrl);

        // 1.1 安全扫描（skipScan=true 时跳过扫描直接安装）
        if (!skipScan && bundle.getFiles() != null && !bundle.getFiles().isEmpty()) {
            SecurityScanResult scanResult = securityScanner.scanBundle(bundle.getFiles());
            if (!scanResult.isPassed()) {
                throw new SecurityScanException(scanResult);
            }
        }

        // 2. 分析 zip 文件结构，按 skill 目录分组
        Map<String, Map<String, byte[]>> skillFilesMap = groupFilesBySkill(bundle.getFiles());

        // 3. 每个 skill 目录创建一条 SkillConfig
        List<Long> skillIds = new ArrayList<>();
        for (Map.Entry<String, Map<String, byte[]>> entry : skillFilesMap.entrySet()) {
            String skillName = entry.getKey();
            Map<String, byte[]> files = entry.getValue();
            String content = findSkillContent(files);
            Long skillId = createSkillConfig(skillName, content, null, bundleUrl, files);
            skillIds.add(skillId);
        }

        return skillIds;
    }

    /**
     * 将 zip 文件列表按 skill 目录分组
     *
     * 单技能 zip 结构：glmv-stock-analyst/SKILL.md, glmv-stock-analyst/scripts/...
     * 技能集合 zip 结构：skills/glmv-stock-analyst/SKILL.md, skills/glmv-web-replication/SKILL.md, ...
     *
     * @return Map<skillName, Map<relativePath, fileContent>>
     */
    private Map<String, Map<String, byte[]>> groupFilesBySkill(Map<String, byte[]> allFiles) {
        Map<String, Map<String, byte[]>> result = new LinkedHashMap<>();
        if (allFiles == null || allFiles.isEmpty()) return result;

        for (Map.Entry<String, byte[]> entry : allFiles.entrySet()) {
            String zipPath = entry.getKey();
            if (shouldSkipFile(zipPath)) continue;

            ParsedFile parsed = parseSkillPath(zipPath);
            if (parsed == null) continue;

            result.computeIfAbsent(parsed.skillName, k -> new LinkedHashMap<>())
                  .put(parsed.relativePath, entry.getValue());
        }

        return result;
    }

    /**
     * 解析 zip 路径，提取 skill 名和相对路径
     *
     * 单技能路径：glmv-stock-analyst/SKILL.md → skillName=glmv-stock-analyst, relative=SKILL.md
     * 技能集合：skills/glmv-stock-analyst/SKILL.md → skillName=glmv-stock-analyst, relative=SKILL.md
     */
    private ParsedFile parseSkillPath(String zipPath) {
        if (zipPath == null || zipPath.isEmpty()) return null;

        // 去掉 GLM-skills-main/ 前缀（如果存在）
        if (zipPath.startsWith("GLM-skills-main/")) {
            zipPath = zipPath.substring("GLM-skills-main/".length());
        }

        // 去掉 skills/ 前缀，确定 skill 名
        String skillName;
        String relative;
        if (zipPath.startsWith("skills/")) {
            // 技能集合格式：skills/glmv-xxx/...
            zipPath = zipPath.substring("skills/".length());
            int slash = zipPath.indexOf('/');
            if (slash < 0) return null;
            skillName = zipPath.substring(0, slash);
            relative = zipPath.substring(slash + 1);
        } else {
            // 单技能格式：glmv-xxx/...（直接是 skill 目录）
            int slash = zipPath.indexOf('/');
            if (slash < 0) return null;
            skillName = zipPath.substring(0, slash);
            relative = zipPath.substring(slash + 1);
        }

        if (skillName.isEmpty() || relative.isEmpty()) return null;
        return new ParsedFile(skillName, relative);
    }

    /**
     * 判断是否跳过该文件
     */
    private boolean shouldSkipFile(String zipPath) {
        if (zipPath == null) return true;
        return zipPath.contains("__pycache__")
                || zipPath.contains(".DS_Store")
                || zipPath.contains(".github/")
                || zipPath.endsWith(".pyc")
                || zipPath.endsWith(".gitkeep");
    }

    /**
     * 从文件列表中找到 SKILL.md 内容
     */
    private String findSkillContent(Map<String, byte[]> files) {
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            String path = entry.getKey().toUpperCase();
            if (path.endsWith("SKILL.MD") || path.endsWith("SKILL.MARKDOWN")) {
                return new String(entry.getValue(), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /**
     * 创建单个 SkillConfig：保存记录 + 上传文件到 S3
     *
     * @return 新创建 skill 的 ID
     */
    private Long createSkillConfig(String name, String content, String description,
                                   String sourceUrl, Map<String, byte[]> files) {
        SkillConfigEntity skill = new SkillConfigEntity();
        skill.setName(name);
        skill.setDescription(description);
        skill.setContent(content);
        skill.setSource("HUB");
        skill.setSourceUrl(sourceUrl);
        skill.setEnabled(0);
        skillConfigInfra.save(skill);

        String workspacePath = "skills/" + skill.getId() + "/";
        skill.setWorkspacePath(workspacePath);
        skillConfigInfra.updateById(skill);

        // 上传所有文件
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            String filePath = workspacePath + entry.getKey();
            byte[] fileContent = entry.getValue();
            String contentType = getContentType(entry.getKey());
            fileStorageUtil.upload(filePath, new ByteArrayInputStream(fileContent), fileContent.length, contentType);
        }

        return skill.getId();
    }

    /**
     * 根据文件扩展名获取 Content-Type
     */
    private String getContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".md")) return "text/markdown; charset=utf-8";
        if (lower.endsWith(".txt")) return "text/plain; charset=utf-8";
        if (lower.endsWith(".json")) return "application/json; charset=utf-8";
        if (lower.endsWith(".yaml") || lower.endsWith(".yml")) return "application/x-yaml; charset=utf-8";
        if (lower.endsWith(".xml")) return "application/xml; charset=utf-8";
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }

    /** 解析结果 */
    private record ParsedFile(String skillName, String relativePath) {}
}
