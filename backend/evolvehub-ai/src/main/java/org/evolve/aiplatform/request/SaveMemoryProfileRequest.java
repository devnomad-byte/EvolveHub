package org.evolve.aiplatform.request;

/**
 * 保存用户画像请求
 *
 * @param targetUserId    目标用户 ID，为空时默认当前用户
 * @param markdownContent 画像 Markdown 内容
 * @author TellyJiang
 * @since 2026-04-17
 */
public record SaveMemoryProfileRequest(
        Long targetUserId,
        String markdownContent) {
}
