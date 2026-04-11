package org.evolve.aiplatform.bean.dto.request;

import jakarta.validation.constraints.NotNull;
import org.evolve.aiplatform.bean.enums.KbLevel;

/**
 * 更新知识库请求
 *
 * @param id          知识库 ID
 * @param name        知识库名称
 * @param level       权限级别（GLOBAL/DEPT/PROJECT/SENSITIVE）
 * @param deptId      所属部门 ID（DEPT 和 PROJECT 级必填）
 * @param description 知识库描述
 */
public record UpdateKbRequest(
        @NotNull(message = "知识库ID不能为空") Long id,
        String name,
        KbLevel level,
        Long deptId,
        String description) {
}