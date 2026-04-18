package org.evolve.aiplatform.memory.infrastructure.support;

import cn.hutool.core.util.StrUtil;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;

/**
 * Memory Markdown 渲染工具
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
public class MemoryMarkdownRenderer {

    /**
     * 渲染默认 Memory 模板
     *
     * @param userId 用户 ID
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    public String renderDefaultTemplate(Long userId) {
        MemoryProfileDTO profile = new MemoryProfileDTO(userId, "", "", "", "", "", "");
        return render(profile);
    }

    /**
     * 将结构化画像渲染为 Markdown
     *
     * @param profile 画像对象
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    public String render(MemoryProfileDTO profile) {
        StringBuilder builder = new StringBuilder();
        builder.append("# 基本信息\n")
                .append("- 姓名：").append(defaultValue(profile.getName())).append("\n")
                .append("- 部门：").append(defaultValue(profile.getDepartment())).append("\n")
                .append("- 语言：").append(defaultValue(profile.getLanguage())).append("\n\n")
                .append("# 偏好设置\n")
                .append("- 偏好模型：").append(defaultValue(profile.getPreferredModel())).append("\n\n")
                .append("# 工具设置\n")
                .append("- 工具偏好：").append(defaultValue(profile.getToolPreference())).append("\n");
        return builder.toString();
    }

    private String defaultValue(String value) {
        return StrUtil.blankToDefault(value, "未设置");
    }
}
