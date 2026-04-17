package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新技能配置请求
 *
 * @param id          技能配置 ID（必填）
 * @param name        技能名称
 * @param description 技能描述
 * @param skillType   技能类型
 * @param content     SKILL.md 内容
 * @param source      来源
 * @param sourceUrl   来源 URL
 * @param tags        标签
 * @param config      配置信息
 * @param enabled     启用状态
 * @param scope       资源范围
 * @param deptId      部门ID
 */
public record UpdateSkillConfigRequest(
        @NotNull(message = "技能配置ID不能为空") Long id,
        String name,
        String description,
        String skillType,
        String content,
        String source,
        String sourceUrl,
        String tags,
        String config,
        Integer enabled,
        String scope,
        Long deptId) {
}
