package org.evolve.aiplatform.bean.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.evolve.aiplatform.bean.enums.KbLevel;

/**
 * 创建知识库请求
 *
 * @param name        知识库名称
 * @param level       权限级别（GLOBAL/DEPT/PROJECT/SENSITIVE）
 * @param deptId      所属部门 ID（DEPT 和 PROJECT 级必填）
 * @param ownerId     所有者用户 ID
 * @param description 知识库描述
 */
public record CreateKbRequest(
        @NotBlank(message = "知识库名称不能为空") String name,
        @NotNull(message = "权限级别不能为空") KbLevel level,
        Long deptId,
        Long ownerId,
        String description) {
}