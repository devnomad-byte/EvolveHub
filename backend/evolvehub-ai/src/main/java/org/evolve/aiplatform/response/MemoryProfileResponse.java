package org.evolve.aiplatform.response;

/**
 * 用户画像返回对象
 *
 * @param userId          目标用户 ID
 * @param markdownContent 画像 Markdown 内容
 * @author TellyJiang
 * @since 2026-04-17
 */
public record MemoryProfileResponse(
        Long userId,
        String markdownContent) {
}
