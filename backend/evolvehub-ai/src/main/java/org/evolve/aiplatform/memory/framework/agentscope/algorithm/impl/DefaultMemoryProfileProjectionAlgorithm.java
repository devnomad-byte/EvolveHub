package org.evolve.aiplatform.memory.framework.agentscope.algorithm.impl;

import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryImportanceAlgorithm;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryProfileProjectionAlgorithm;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认画像投影算法
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultMemoryProfileProjectionAlgorithm implements MemoryProfileProjectionAlgorithm {

    private final MemoryImportanceAlgorithm memoryImportanceAlgorithm;

    public DefaultMemoryProfileProjectionAlgorithm(MemoryImportanceAlgorithm memoryImportanceAlgorithm) {
        this.memoryImportanceAlgorithm = memoryImportanceAlgorithm;
    }

    @Override
    public List<MemoryStructuredItemDTO> projectStructuredItems(MemoryProfileDTO profile) {
        List<MemoryStructuredItemDTO> items = new ArrayList<>();
        if (profile == null) {
            return items;
        }
        addFact(items, profile, "profile.name", profile.getName());
        addFact(items, profile, "profile.department", profile.getDepartment());
        addPreference(items, profile, "profile.language", profile.getLanguage());
        addPreference(items, profile, "profile.model", profile.getPreferredModel());
        addToolConfig(items, profile, "profile.tools", profile.getToolPreference());
        return items;
    }

    @Override
    public String buildProfileSummary(MemoryProfileDTO profile) {
        if (profile == null) {
            return "";
        }
        return String.join(" / ",
                safe(profile.getName()),
                safe(profile.getDepartment()),
                safe(profile.getLanguage()),
                safe(profile.getPreferredModel()),
                safe(profile.getToolPreference()));
    }

    private void addPreference(List<MemoryStructuredItemDTO> items, MemoryProfileDTO profile, String key, String content) {
        addItem(items, profile, key, MemoryConstants.MEMORY_TYPE_PREFERENCE, content);
    }

    private void addFact(List<MemoryStructuredItemDTO> items, MemoryProfileDTO profile, String key, String content) {
        addItem(items, profile, key, MemoryConstants.MEMORY_TYPE_FACT, content);
    }

    private void addToolConfig(List<MemoryStructuredItemDTO> items, MemoryProfileDTO profile, String key, String content) {
        addItem(items, profile, key, MemoryConstants.MEMORY_TYPE_TOOL_CONFIG, content);
    }

    private void addItem(List<MemoryStructuredItemDTO> items,
                         MemoryProfileDTO profile,
                         String key,
                         String type,
                         String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        items.add(new MemoryStructuredItemDTO(
                profile.getUserId(),
                key,
                type,
                content,
                memoryImportanceAlgorithm.scoreProfileField(profile, key, content)
        ));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
