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
import java.util.Map;

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
    public Long installFromHub(String hubName, String bundleUrl) {
        return installFromHub(hubName, bundleUrl, false);
    }

    @Transactional
    public Long installFromHub(String hubName, String bundleUrl, boolean skipScan) {
        // 1. 下载并解析技能包
        HubSkillBundle bundle = hubManager.downloadBundle(hubName, bundleUrl);

        // 1.1 安全扫描（skipScan 仅允许跳过 MEDIUM/LOW）
        if (bundle.getFiles() != null && !bundle.getFiles().isEmpty()) {
            SecurityScanResult scanResult = securityScanner.scanBundle(bundle.getFiles());
            if (!scanResult.isPassed()) {
                if (!skipScan || !securityScanner.canBypass(scanResult)) {
                    throw new SecurityScanException(scanResult);
                }
            }
        }
        if (bundle.getContent() != null) {
            SecurityScanResult scanResult = securityScanner.scanText(bundle.getContent(), "SKILL.md");
            if (!scanResult.isPassed()) {
                if (!skipScan || !securityScanner.canBypass(scanResult)) {
                    throw new SecurityScanException(scanResult);
                }
            }
        }

        // 2. 创建技能记录
        SkillConfigEntity skill = new SkillConfigEntity();
        skill.setName(bundle.getName());
        skill.setDescription(bundle.getReadme());
        skill.setContent(bundle.getContent());
        skill.setSource("HUB");
        skill.setSourceUrl(bundleUrl);
        skill.setEnabled(0);  // 默认禁用，待配置
        skillConfigInfra.save(skill);

        // 3. 确定工作区路径并更新
        String workspacePath = "skills/" + skill.getId() + "/";
        skill.setWorkspacePath(workspacePath);
        skillConfigInfra.updateById(skill);

        // 4. 上传所有文件到 S3/本地存储
        if (bundle.getFiles() != null && !bundle.getFiles().isEmpty()) {
            for (Map.Entry<String, byte[]> entry : bundle.getFiles().entrySet()) {
                String filePath = workspacePath + entry.getKey();
                byte[] fileContent = entry.getValue();
                String contentType = getContentType(entry.getKey());
                fileStorageUtil.upload(filePath, new ByteArrayInputStream(fileContent), fileContent.length, contentType);
            }
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
}
