package org.evolve.aiplatform.memory.application.service;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;

/**
 * 用户画像记忆服务接口
 * 
 * 负责用户画像记忆的初始化、读取与更新，是画像类记忆能力的统一扩展点。
 * 对外定义稳定契约，具体渲染、存储和结构化同步策略由实现类负责。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
public interface MemoryProfileService {

    /**
     * 初始化用户画像记忆
     *
     * @param userId 用户 ID
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-12
     */
    String initializeMemoryProfile(Long userId);

    /**
     * 获取用户画像记忆
     *
     * @param userId 用户 ID
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-12
     */
    String getMemoryProfile(Long userId);

    /**
     * 更新用户画像记忆
     *
     * @param profile 画像对象
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-12
     */
    String updateMemoryProfile(MemoryProfileDTO profile);

    /**
     * 直接保存用户画像 Markdown
     *
     * @param userId 用户 ID
     * @param markdownContent Markdown 内容
     * @return 保存后的 Markdown 内容
     * @author TellyJiang
     * @since 2026-04-15
     */
    String saveMemoryProfileMarkdown(Long userId, String markdownContent);

    /**
     * 追加用户画像 Markdown
     *
     * @param userId 用户 ID
     * @param content 要追加的内容
     * @return 更新后的 Markdown 内容
     * @author TellyJiang
     * @since 2026-04-15
     */
    String appendMemoryProfileMarkdown(Long userId, String content);
}
