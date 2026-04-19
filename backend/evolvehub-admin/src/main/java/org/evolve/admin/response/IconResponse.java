package org.evolve.admin.response;

import org.evolve.domain.resource.model.DesktopIconEntity;

public record IconResponse(
    Long id,
    Long permId,
    Long categoryId,
    Integer isDesktop,
    Integer sort,
    String permCode,
    String menuName,
    String icon,
    String gradient
) {
    public static IconResponse from(DesktopIconEntity entity, String permCode, String menuName, String icon, String gradient) {
        return new IconResponse(
            entity.getId(),
            entity.getPermId(),
            entity.getCategoryId(),
            entity.getIsDesktop(),
            entity.getSort(),
            permCode,
            menuName,
            icon,
            gradient
        );
    }
}
