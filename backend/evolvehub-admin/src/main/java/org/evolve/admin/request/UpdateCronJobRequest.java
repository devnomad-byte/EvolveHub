package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCronJobRequest(
    Long id,

    @Size(max = 128, message = "任务名称最多128字符")
    String name,

    @Size(max = 512, message = "任务描述最多512字符")
    String description,

    String cronExpression,

    String timezone,

    String taskType,

    String promptTemplate,

    Long targetUserId,

    Long targetSessionId,

    Integer timeoutSeconds,

    Integer maxRetries,

    Integer misfireGraceSeconds,

    Integer maxConcurrency,

    Integer enabled,

    Long deptId
) {}
