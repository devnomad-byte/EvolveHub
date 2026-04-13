package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建系统级技能配置请求
 *
 * @param name        技能名称
 * @param description 技能描述
 * @param skillType   技能类型
 * @param config      配置信息（JSON 字符串）
 * @param enabled     启用状态（1=启用 0=禁用）
 */
public record CreateSkillConfigRequest(
        @NotBlank(message = "技能名称不能为空") String name,
        String description,
        String skillType,
        String config,
        @NotNull(message = "启用状态不能为空") Integer enabled) {
}
