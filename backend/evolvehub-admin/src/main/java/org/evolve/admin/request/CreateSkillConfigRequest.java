package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建技能配置请求
 *
 * @param name        技能名称
 * @param description 技能描述
 * @param skillType   技能类型（CODER / WRITER / ANALYST 等）
 * @param content     SKILL.md 内容（Markdown）
 * @param source      来源：MANUAL / HUB / BUILTIN
 * @param sourceUrl   Hub 安装时的来源 URL
 * @param tags        标签数组
 * @param config      配置信息（JSON）
 * @param enabled     启用状态
 * @param scope       资源范围：SYSTEM / DEPT / USER
 * @param deptId      部门ID（scope=DEPT 时必填）
 */
public record CreateSkillConfigRequest(
        @NotBlank(message = "技能名称不能为空") String name,
        String description,
        String skillType,
        String content,
        String source,
        String sourceUrl,
        String tags,
        String config,
        @NotNull(message = "启用状态不能为空") Integer enabled,
        String scope,
        Long deptId) {
}
