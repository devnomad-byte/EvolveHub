package org.evolve.admin.response;

import org.evolve.domain.resource.model.CronJobEntity;

import java.time.LocalDateTime;

public record CronJobResponse(
    Long id,
    String name,
    String description,
    Integer enabled,
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
    LocalDateTime lastRunTime,
    LocalDateTime nextRunTime,
    String lastRunStatus,
    String lastRunError,
    Long deptId,
    Long createBy,
    LocalDateTime createTime,
    LocalDateTime updateTime
) {
    public static CronJobResponse from(CronJobEntity e) {
        return new CronJobResponse(
            e.getId(), e.getName(), e.getDescription(), e.getEnabled(),
            e.getCronExpression(), e.getTimezone(), e.getTaskType(),
            e.getPromptTemplate(), e.getTargetUserId(), e.getTargetSessionId(),
            e.getTimeoutSeconds(), e.getMaxRetries(), e.getMisfireGraceSeconds(),
            e.getMaxConcurrency(), e.getLastRunTime(), e.getNextRunTime(),
            e.getLastRunStatus(), e.getLastRunError(), e.getDeptId(),
            e.getCreateBy(), e.getCreateTime(), e.getUpdateTime()
        );
    }
}
