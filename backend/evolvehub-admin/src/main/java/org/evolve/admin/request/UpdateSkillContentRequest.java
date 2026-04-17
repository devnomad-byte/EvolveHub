package org.evolve.admin.request;

/**
 * 更新技能内容请求
 *
 * @param content SKILL.md 内容（Markdown）
 */
public record UpdateSkillContentRequest(String content) {
}
