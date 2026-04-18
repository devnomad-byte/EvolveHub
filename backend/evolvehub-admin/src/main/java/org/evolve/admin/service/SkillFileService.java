package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.utils.FileStorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.admin.response.FileNode;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.zip.ZipInputStream;

@Service
public class SkillFileService {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Resource
    private FileStorageUtil fileStorageUtil;

    /**
     * 列出技能工作区文件
     * 直接从 S3 读取目录结构，支持子目录导航
     * @param skillId 技能 ID
     * @param path 子目录路径，如 "scripts/"，为空表示根目录
     */
    public List<FileNode> listFiles(Long skillId, String path) {
        String workspacePath = skillConfigInfra.getWorkspacePath(skillId);
        if (workspacePath == null || workspacePath.isEmpty()) {
            return Collections.emptyList();
        }

        // 拼接完整 prefix
        String prefix = workspacePath;
        if (path != null && !path.isEmpty()) {
            prefix = prefix + path;
        }

        FileStorageUtil.ListObjectsResult result = fileStorageUtil.listObjectsWithDelimiter(
                fileStorageUtil.getBucket(), prefix);

        String basePath = workspacePath;
        List<FileNode> nodes = new java.util.ArrayList<>();

        // 添加文件夹
        for (String folder : result.getFolders()) {
            // 文件夹路径形如 skills/{id}/scripts/
            // 提取相对于 workspacePath 的部分
            String relative = folder.substring(basePath.length());
            // 去掉末尾 /
            if (relative.endsWith("/")) {
                relative = relative.substring(0, relative.length() - 1);
            }
            nodes.add(FileNode.builder()
                    .name(relative)
                    .path(relative)
                    .type("folder")
                    .size(0L)
                    .modifiedTime(null)
                    .build());
        }

        // 添加文件
        for (FileStorageUtil.FileSummary f : result.getFiles()) {
            String key = f.getKey();
            // 提取相对于 workspacePath 的部分
            String relative = key.substring(basePath.length());
            // 跳过文件夹自身
            if (relative.isEmpty()) continue;
            nodes.add(FileNode.builder()
                    .name(relative)
                    .path(relative)
                    .type("file")
                    .size(f.getSize())
                    .modifiedTime(f.getLastModified())
                    .build());
        }

        return nodes;
    }

    /**
     * 上传文件到技能工作区
     * - ZIP 文件：解析并按 skill 子目录分组，上传文件到 S3，更新数据库 content
     * - SKILL.md：直接写入 S3 + 更新数据库 content
     * - 其他文件：仅写入 S3
     */
    public void uploadFile(Long skillId, String filePath, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Missing filename");
            }

            // 判断是否为 ZIP 文件
            if (originalFilename.toLowerCase().endsWith(".zip")) {
                uploadZipAsSkill(skillId, file);
            } else {
                // 非 ZIP：写入 S3 存储
                String storageKey = buildStorageKey(skillId, filePath, originalFilename);
                fileStorageUtil.upload(storageKey, file.getInputStream(), file.getSize(), file.getContentType());

                // 如果是 SKILL.md（忽略大小写），同时更新数据库 content
                if (originalFilename.equalsIgnoreCase("SKILL.md")) {
                    String contentText = new String(file.getBytes(), StandardCharsets.UTF_8);
                    contentText = cleanUtf8Content(contentText);
                    SkillConfigEntity skill = new SkillConfigEntity();
                    skill.setId(skillId);
                    skill.setContent(contentText);
                    skillConfigInfra.updateSkillConfig(skill);
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file: " + e.getMessage());
        }
    }

    /**
     * 上传 ZIP 包作为技能
     * 解析逻辑与 installFromHub 完全一致：按 skill 子目录分组，提取 SKILL.md content，
     * 上传所有文件到 S3，更新数据库 content 字段
     */
    private void uploadZipAsSkill(Long skillId, MultipartFile file) throws IOException {
        byte[] zipBytes = file.getBytes();
        if (zipBytes == null || zipBytes.length == 0) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Empty zip file");
        }

        // 1. 解析 ZIP，按 skill 目录分组
        Map<String, Map<String, byte[]>> skillFilesMap = groupZipFilesBySkill(zipBytes);

        // 2. 获取或创建 workspacePath
        String workspacePath = skillConfigInfra.getWorkspacePath(skillId);
        if (workspacePath == null || workspacePath.isEmpty()) {
            workspacePath = "skills/" + skillId + "/";
        }
        if (!workspacePath.endsWith("/")) {
            workspacePath += "/";
        }

        // 3. 上传所有文件到 S3
        for (Map.Entry<String, Map<String, byte[]>> skillEntry : skillFilesMap.entrySet()) {
            Map<String, byte[]> files = skillEntry.getValue();
            for (Map.Entry<String, byte[]> fileEntry : files.entrySet()) {
                String relativePath = fileEntry.getKey();
                byte[] fileContent = fileEntry.getValue();
                String storageKey = workspacePath + relativePath;
                String contentType = getContentType(relativePath);
                fileStorageUtil.upload(storageKey, new ByteArrayInputStream(fileContent), fileContent.length, contentType);
            }
        }

        // 4. 找到 SKILL.md content 并更新数据库
        for (Map.Entry<String, Map<String, byte[]>> skillEntry : skillFilesMap.entrySet()) {
            Map<String, byte[]> files = skillEntry.getValue();
            String content = findSkillContentFromFiles(files);
            if (content != null) {
                SkillConfigEntity skill = new SkillConfigEntity();
                skill.setId(skillId);
                skill.setContent(content);
                skillConfigInfra.updateSkillConfig(skill);
                break;
            }
        }
    }

    /**
     * 解析 ZIP 文件，按 skill 子目录分组
     * 逻辑与 SkillInstallService.groupFilesBySkill 完全一致
     */
    private Map<String, Map<String, byte[]>> groupZipFilesBySkill(byte[] zipData) {
        Map<String, Map<String, byte[]>> result = new LinkedHashMap<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory() || entry.getName() == null) {
                    zis.closeEntry();
                    continue;
                }

                String zipPath = entry.getName();

                // 跳过无效路径
                if (shouldSkipZipPath(zipPath)) {
                    zis.closeEntry();
                    continue;
                }

                // 解析 skill 路径
                ParsedZipPath parsed = parseZipSkillPath(zipPath);
                if (parsed == null) {
                    zis.closeEntry();
                    continue;
                }

                byte[] content = zis.readAllBytes();
                result.computeIfAbsent(parsed.skillName, k -> new LinkedHashMap<>())
                      .put(parsed.relativePath, content);

                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse zip: " + e.getMessage());
        }

        return result;
    }

    /**
     * 判断 ZIP 条目是否跳过
     */
    private boolean shouldSkipZipPath(String zipPath) {
        if (zipPath == null) return true;
        return zipPath.contains("__pycache__")
                || zipPath.contains(".DS_Store")
                || zipPath.contains(".github/")
                || zipPath.endsWith(".pyc")
                || zipPath.endsWith(".gitkeep");
    }

    /**
     * 解析 ZIP 路径，提取 skill 名和相对路径
     * 逻辑与 SkillInstallService.parseSkillPath 完全一致
     */
    private ParsedZipPath parseZipSkillPath(String zipPath) {
        // 去掉 GLM-skills-main/ 前缀（如果存在）
        if (zipPath.startsWith("GLM-skills-main/")) {
            zipPath = zipPath.substring("GLM-skills-main/".length());
        }

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
        return new ParsedZipPath(skillName, relative);
    }

    /**
     * 从文件列表中找到 SKILL.md 内容（UTF-8 解析并清理 null byte）
     */
    private String findSkillContentFromFiles(Map<String, byte[]> files) {
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            String path = entry.getKey().toUpperCase();
            if (path.endsWith("SKILL.MD") || path.endsWith("SKILL.MARKDOWN")) {
                String text = new String(entry.getValue(), StandardCharsets.UTF_8);
                return cleanUtf8Content(text);
            }
        }
        return null;
    }

    /**
     * 清理 UTF-8 内容中的无效字符（PostgreSQL TEXT 不允许 0x00）
     */
    private String cleanUtf8Content(String text) {
        if (text == null) return null;
        return text.replace("\u0000", "");
    }

    /**
     * 下载文件
     * SKILL.md 直接从数据库 content 读取（ModelScope 等 Hub 安装时内容已存库）
     * 其他文件从 S3 读取
     */
    public byte[] downloadFile(Long skillId, String filePath) {
        try {
            // SKILL.md 从数据库读取，避免 S3 路径拼接不一致的问题
            if ("SKILL.md".equals(filePath)) {
                SkillConfigEntity skill = skillConfigInfra.getById(skillId);
                if (skill != null && skill.getContent() != null) {
                    return skill.getContent().getBytes(StandardCharsets.UTF_8);
                }
            }
            // 其他文件从 S3 读取
            String storageKey = buildStorageKey(skillId, null, filePath);
            return fileStorageUtil.download(storageKey);
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "File not found: " + filePath);
        }
    }

    /**
     * 删除文件
     * 从 S3 删除
     */
    public void deleteFile(Long skillId, String filePath) {
        try {
            String storageKey = buildStorageKey(skillId, null, filePath);
            fileStorageUtil.delete(storageKey);

            // 如果是 SKILL.md，清空数据库内容
            if ("SKILL.md".equals(filePath)) {
                SkillConfigEntity skill = new SkillConfigEntity();
                skill.setId(skillId);
                skill.setContent(null);
                skillConfigInfra.updateSkillConfig(skill);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * 创建文件夹
     * 在 S3 创建空对象作为文件夹标记
     */
    public void createFolder(Long skillId, String folderPath) {
        try {
            String storageKey = buildStorageKey(skillId, folderPath, null);
            // 确保路径以 / 结尾
            if (!storageKey.endsWith("/")) {
                storageKey += "/";
            }
            // 上传空内容作为文件夹标记
            fileStorageUtil.upload(storageKey, new ByteArrayInputStream(new byte[0]), 0, "application/x-directory");
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create folder: " + e.getMessage());
        }
    }

    /**
     * 构建存储路径
     */
    private String buildStorageKey(Long skillId, String path, String filename) {
        String basePath = skillConfigInfra.getWorkspacePath(skillId);
        if (path == null || path.isEmpty()) {
            return filename != null ? basePath + filename : basePath;
        }
        if (path.endsWith("/")) {
            return filename != null ? basePath + path + filename : basePath + path;
        }
        return basePath + path;
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

    /** 解析 ZIP 路径结果 */
    private record ParsedZipPath(String skillName, String relativePath) {}
}
