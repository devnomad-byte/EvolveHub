package org.evolve.aiplatform.memory.framework.agentscope.algorithm.impl;

import io.agentscope.core.message.Msg;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryDeduplicationAlgorithm;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 默认记忆去重算法
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultMemoryDeduplicationAlgorithm implements MemoryDeduplicationAlgorithm {

    @Override
    public String buildMemoryKey(Long userId, String sessionId, Msg message) {
        if (message == null) {
            return "memory-" + safe(sessionId) + "-empty";
        }
        if (message.getId() != null && !message.getId().isBlank()) {
            return safe(sessionId) + "-" + sanitize(message.getId());
        }
        String raw = safe(sessionId)
                + "|"
                + (message.getRole() == null ? "" : message.getRole().name())
                + "|"
                + safe(message.getTimestamp())
                + "|"
                + safe(message.getTextContent())
                + "|"
                + safe(userId == null ? null : String.valueOf(userId));
        return safe(sessionId) + "-" + sha256(raw);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9\\-]", "");
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : hash) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 算法不可用", exception);
        }
    }
}
