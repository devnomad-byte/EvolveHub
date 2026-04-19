package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCronJobRequest(
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 128, message = "任务名称最多128字符")
    String name,

    @Size(max = 512, message = "任务描述最多512字符")
    String description,

    @NotBlank(message = "cron表达式不能为空")
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

    Long deptId
) {}
