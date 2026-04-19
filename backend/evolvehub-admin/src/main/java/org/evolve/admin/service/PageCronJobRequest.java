package org.evolve.admin.service;

public record PageCronJobRequest(
    Integer pageNum,
    Integer pageSize,
    Long targetUserId,
    Integer enabled
) {}
