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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class SkillFileService {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Resource
    private FileStorageUtil fileStorageUtil;

    /**
     * 列出技能工作区文件
     * 从数据库读取
     */
    public List<FileNode> listFiles(Long skillId) {
        SkillConfigEntity skill = skillConfigInfra.getById(skillId);
        if (skill == null || skill.getContent() == null || skill.getContent().isEmpty()) {
            return Collections.emptyList();
        }
        // 目前只显示 SKILL.md
        FileNode node = FileNode.builder()
            .name("SKILL.md")
            .path("SKILL.md")
            .type("file")
            .size((long) skill.getContent().getBytes(StandardCharsets.UTF_8).length)
            .modifiedTime(skill.getUpdateTime() != null ? skill.getUpdateTime().toString() : null)
            .build();
        return Collections.singletonList(node);
    }

    /**
     * 上传文件到技能工作区
     * 写入数据库 + S3 存储
     */
    public void uploadFile(Long skillId, String filePath, MultipartFile file) {
        try {
            // 写入 S3 存储
            String storageKey = buildStorageKey(skillId, filePath, file.getOriginalFilename());
            fileStorageUtil.upload(storageKey, file.getInputStream(), file.getSize(), file.getContentType());

            // 如果是 SKILL.md，同时更新数据库
            if ("SKILL.md".equals(file.getOriginalFilename())) {
                SkillConfigEntity skill = new SkillConfigEntity();
                skill.setId(skillId);
                skill.setContent(new String(file.getBytes(), StandardCharsets.UTF_8));
                skillConfigInfra.updateSkillConfig(skill);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * 从 S3 读取
     */
    public byte[] downloadFile(Long skillId, String filePath) {
        try {
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
}
