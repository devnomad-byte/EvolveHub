package org.evolve.admin.request;

public record UpdateIconRequest(
    Long permId,
    Long categoryId,
    Integer isDesktop,
    Integer sort
) {}
