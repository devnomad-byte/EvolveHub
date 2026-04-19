package org.evolve.admin.service;

public record CronJobHistoryPageRequest(
    Long jobId,
    Integer pageNum,
    Integer pageSize
) {}
