package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.application.service.MemoryProfileService;
import org.evolve.aiplatform.memory.application.service.MemoryStructuredService;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryObjectEntity;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryProfileEntity;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryProfileProjectionAlgorithm;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryObjectRepository;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryProfileRepository;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryMarkdownRenderer;
import org.evolve.aiplatform.utils.S3Util;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户画像记忆服务实现
 * 
 * 负责将用户画像以 Markdown 形式持久化，并在画像更新时同步生成结构化偏好/事实记忆。
 * 该类是画像类记忆的默认实现，组合了渲染器、画像存储和结构化记忆写入能力。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Service
class MemoryProfileServiceImpl implements MemoryProfileService {

    @Resource
    private AgentMemoryProfileRepository agentMemoryProfileRepository;

    @Resource
    private AgentMemoryObjectRepository agentMemoryObjectRepository;

    @Resource(name = "memoryStructuredServiceImpl")
    private MemoryStructuredService memoryStructuredService;

    @Resource
    private MemoryProfileProjectionAlgorithm memoryProfileProjectionAlgorithm;

    @Resource
    private MemoryMarkdownRenderer memoryMarkdownRenderer;

    @Resource(name = "aiS3Util")
    private S3Util s3Util;

    /**
     * 初始化用户画像记忆
     *
     * @param userId 用户 ID
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public String initializeMemoryProfile(Long userId) {
        String markdown = memoryMarkdownRenderer.renderDefaultTemplate(userId);
        saveMemoryProfileMarkdown(userId, markdown);
        return markdown;
    }

    /**
     * 获取用户画像记忆
     *
     * @param userId 用户 ID
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public String getMemoryProfile(Long userId) {
        return agentMemoryProfileRepository.getByUserId(userId)
                .map(this::loadStoredMarkdown)
                .map(markdown -> markdown == null ? "" : markdown)
                .orElseGet(() -> initializeMemoryProfile(userId));
    }

    /**
     * 更新用户画像记忆
     *
     * @param profile 画像对象
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public String updateMemoryProfile(MemoryProfileDTO profile) {
        String content = memoryMarkdownRenderer.render(profile);
        saveProfile(profile, content);
        persistProfileStructuredMemory(profile);
        return content;
    }

    /**
     * 直接保存用户画像 Markdown
     *
     * @param userId 用户 ID
     * @param markdownContent Markdown 内容
     * @return 保存后的 Markdown 内容
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public String saveMemoryProfileMarkdown(Long userId, String markdownContent) {
        String normalizedContent = normalizeMarkdown(markdownContent);
        AgentMemoryProfileEntity entity = agentMemoryProfileRepository.getByUserId(userId)
                .orElseGet(AgentMemoryProfileEntity::new);
        entity.setUserId(userId);
        entity.setLastExtractedTime(LocalDateTime.now());
        entity.setProfileSummary(buildMarkdownSummary(normalizedContent));
        saveProfileObject(entity, normalizedContent);
        agentMemoryProfileRepository.saveOrUpdateEntity(entity);
        return normalizedContent;
    }

    /**
     * 追加用户画像 Markdown
     *
     * @param userId 用户 ID
     * @param content 要追加的内容
     * @return 更新后的 Markdown 内容
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public String appendMemoryProfileMarkdown(Long userId, String content) {
        String existing = loadStoredMarkdown(userId);
        String appendContent = normalizeMarkdown(content);
        if (existing == null || existing.isBlank()) {
            return saveMemoryProfileMarkdown(userId, appendContent);
        }
        if (appendContent.isBlank()) {
            return existing;
        }
        return saveMemoryProfileMarkdown(userId, existing + System.lineSeparator() + System.lineSeparator() + appendContent);
    }

    /**
     * 将用户画像同步写入结构化记忆表
     *
     * @param profile 用户画像
     * @author TellyJiang
     * @since 2026-04-11
     */
    private void persistProfileStructuredMemory(MemoryProfileDTO profile) {
        List<MemoryStructuredItemDTO> structuredItems = memoryProfileProjectionAlgorithm.projectStructuredItems(profile);
        structuredItems.forEach(memoryStructuredService::upsertMemoryStructuredItem);
    }

    private MemoryProfileDTO toProfileDto(AgentMemoryProfileEntity profileEntity) {
        String markdownContent = loadStoredMarkdown(profileEntity);
        return new MemoryProfileDTO(
                profileEntity.getUserId(),
                profileEntity.getName(),
                profileEntity.getDepartment(),
                profileEntity.getPreferredLanguage(),
                profileEntity.getPreferredModel(),
                profileEntity.getToolPreference(),
                markdownContent
        );
    }

    private String loadStoredMarkdown(Long userId) {
        return agentMemoryProfileRepository.getByUserId(userId)
                .map(this::loadStoredMarkdown)
                .orElse(null);
    }

    private String loadStoredMarkdown(AgentMemoryProfileEntity profileEntity) {
        String markdownContent = "";
        if (profileEntity.getProfileObjectId() != null) {
            markdownContent = agentMemoryObjectRepository.getById(profileEntity.getProfileObjectId())
                    .map(object -> new String(s3Util.download(object.getObjectKey()), StandardCharsets.UTF_8))
                    .orElse("");
        }
        return markdownContent;
    }

    private void saveProfile(MemoryProfileDTO profile, String markdown) {
        AgentMemoryProfileEntity entity = agentMemoryProfileRepository.getByUserId(profile.getUserId())
                .orElseGet(AgentMemoryProfileEntity::new);
        entity.setUserId(profile.getUserId());
        entity.setProfileSummary(memoryProfileProjectionAlgorithm.buildProfileSummary(profile));
        entity.setName(profile.getName());
        entity.setDepartment(profile.getDepartment());
        entity.setPreferredLanguage(profile.getLanguage());
        entity.setPreferredModel(profile.getPreferredModel());
        entity.setToolPreference(profile.getToolPreference());
        entity.setLastExtractedTime(LocalDateTime.now());
        saveProfileObject(entity, markdown);
        agentMemoryProfileRepository.saveOrUpdateEntity(entity);
    }

    private void saveProfileObject(AgentMemoryProfileEntity profileEntity, String markdown) {
        AgentMemoryObjectEntity objectEntity = new AgentMemoryObjectEntity();
        objectEntity.setUserId(profileEntity.getUserId());
        objectEntity.setSessionId(null);
        objectEntity.setObjectType(MemoryConstants.MEMORY_OBJECT_TYPE_PROFILE);
        objectEntity.setBucket(MemoryConstants.MEMORY_DEFAULT_BUCKET);
        objectEntity.setObjectKey("memory/profile/" + profileEntity.getUserId() + "/MEMORY.md");
        objectEntity.setContentType("text/markdown");
        objectEntity.setChecksum(Integer.toHexString(markdown.hashCode()));
        objectEntity.setSizeBytes((long) markdown.getBytes(StandardCharsets.UTF_8).length);
        objectEntity.setVersionNo(1);
        agentMemoryObjectRepository.saveOrUpdateEntity(objectEntity);
        s3Util.upload(objectEntity.getObjectKey(), markdown.getBytes(StandardCharsets.UTF_8), "text/markdown");
        profileEntity.setProfileObjectId(objectEntity.getId());
    }

    private String normalizeMarkdown(String markdownContent) {
        return markdownContent == null ? "" : markdownContent;
    }

    private String buildMarkdownSummary(String markdownContent) {
        if (markdownContent == null || markdownContent.isBlank()) {
            return "";
        }
        String singleLine = markdownContent.replace(System.lineSeparator(), " ")
                .replace("\n", " ")
                .trim();
        if (singleLine.length() <= 120) {
            return singleLine;
        }
        return singleLine.substring(0, 120);
    }
}
