package org.evolve.admin.response;

import org.evolve.domain.resource.model.CronJobHistoryEntity;

import java.time.LocalDateTime;

public record CronJobHistoryResponse(
    Long id,
    Long jobId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String status,
    String triggerType,
    String sessionId,
    String promptContent,
    String responseContent,
    String errorMessage,
    Integer promptTokens,
    Integer completionTokens,
    Integer totalTokens,
    LocalDateTime createTime
) {
    public static CronJobHistoryResponse from(CronJobHistoryEntity e) {
        return new CronJobHistoryResponse(
            e.getId(), e.getJobId(), e.getStartTime(), e.getEndTime(),
            e.getStatus(), e.getTriggerType(), e.getSessionId(),
            e.getPromptContent(), e.getResponseContent(), e.getErrorMessage(),
            e.getPromptTokens(), e.getCompletionTokens(), e.getTotalTokens(),
            e.getCreateTime()
        );
    }
}
