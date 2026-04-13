package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新系统级技能配置请求
 *
 * @param id          技能配置 ID（必填）
 * @param name        技能名称
 * @param description 技能描述
 * @param skillType   技能类型
 * @param config      配置信息（JSON 字符串）
 * @param enabled     启用状态（1=启用 0=禁用）
 */
public record UpdateSkillConfigRequest(
        @NotNull(message = "技能配置ID不能为空") Long id,
        String name, String description, String skillType,
        String config, Integer enabled) {
}
