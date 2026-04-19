package org.evolve.admin.request;

public record UpdateCategoryRequest(
    Long id,
    String name,
    String icon,
    String color,
    Integer sort,
    Integer status
) {}
