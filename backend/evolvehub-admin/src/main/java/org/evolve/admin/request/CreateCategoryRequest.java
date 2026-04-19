package org.evolve.admin.request;

public record CreateCategoryRequest(
    String name,
    String icon,
    String color,
    Integer sort
) {}
