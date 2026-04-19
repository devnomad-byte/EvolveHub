package org.evolve.admin.response;

import org.evolve.domain.resource.model.DesktopCategoryEntity;

public record CategoryResponse(
    Long id,
    String name,
    String icon,
    String color,
    Integer sort,
    Integer status,
    Integer iconCount
) {
    public static CategoryResponse from(DesktopCategoryEntity entity, int iconCount) {
        return new CategoryResponse(
            entity.getId(),
            entity.getName(),
            entity.getIcon(),
            entity.getColor(),
            entity.getSort(),
            entity.getStatus(),
            iconCount
        );
    }
}
