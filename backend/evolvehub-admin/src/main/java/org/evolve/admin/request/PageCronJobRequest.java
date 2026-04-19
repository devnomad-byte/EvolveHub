package org.evolve.admin.request;

public record PageCronJobRequest(
    Integer pageNum,
    Integer pageSize,
    Long targetUserId,
    Integer enabled
) {}
