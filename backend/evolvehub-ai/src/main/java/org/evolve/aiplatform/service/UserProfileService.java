package org.evolve.aiplatform.service;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.utils.S3Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 用户画像文件服务（MinIO 上的 MEMORY.md）
 * <p>
 * 每个用户在 MinIO 上有一个 MEMORY.md 文件，存储用户级别的持久化画像信息，
 * 如个人偏好、工作背景、沟通风格等。该文件在每次对话时注入系统提示词，
 * 帮助 AI 更好地理解用户。
 * </p>
 * <p>
 * 存储路径：users/{userId}/MEMORY.md
 * </p>
 *
 * @author zhao
 */
@Service
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    /**
     * MinIO 对象路径模板
     */
    private static final String PROFILE_KEY_TEMPLATE = "users/%d/MEMORY.md";

    /**
     * 内容类型
     */
    private static final String CONTENT_TYPE = "text/markdown; charset=utf-8";

    @Resource
    private S3Util s3Util;

    /**
     * 读取用户的 MEMORY.md 内容
     *
     * @param userId 用户 ID
     * @return MEMORY.md 文本内容，不存在返回 null
     */
    public String getProfile(Long userId) {
        String key = buildKey(userId);
        try {
            if (!s3Util.exists(key)) {
                return null;
            }
            byte[] data = s3Util.download(key);
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("读取用户画像失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 写入/更新用户的 MEMORY.md
     *
     * @param userId  用户 ID
     * @param content Markdown 内容
     */
    public void saveProfile(Long userId, String content) {
        String key = buildKey(userId);
        try {
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            s3Util.upload(key, data, CONTENT_TYPE);
            log.info("用户画像已更新: userId={}, size={} bytes", userId, data.length);
        } catch (Exception e) {
            log.error("写入用户画像失败: userId={}", userId, e);
        }
    }

    /**
     * 追加内容到用户的 MEMORY.md（不覆盖已有内容）
     *
     * @param userId  用户 ID
     * @param content 要追加的内容
     */
    public void appendProfile(Long userId, String content) {
        String existing = getProfile(userId);
        if (existing == null || existing.isBlank()) {
            saveProfile(userId, content);
        } else {
            saveProfile(userId, existing + "\n\n" + content);
        }
    }

    /**
     * 删除用户的 MEMORY.md
     *
     * @param userId 用户 ID
     */
    public void deleteProfile(Long userId) {
        String key = buildKey(userId);
        try {
            if (s3Util.exists(key)) {
                s3Util.delete(key);
                log.info("用户画像已删除: userId={}", userId);
            }
        } catch (Exception e) {
            log.warn("删除用户画像失败: userId={}", userId, e);
        }
    }

    private String buildKey(Long userId) {
        return String.format(PROFILE_KEY_TEMPLATE, userId);
    }
}

