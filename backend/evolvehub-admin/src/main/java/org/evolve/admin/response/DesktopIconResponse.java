package org.evolve.admin.response;

/**
 * 桌面图标响应
 */
public record DesktopIconResponse(
        Long id,
        String permName,
        String permCode,
        String icon,
        String gradient,
        String path,
        Integer sort,
        Integer status,
        Integer defaultWidth,
        Integer defaultHeight,
        Integer minWidth,
        Integer minHeight,
        Integer dockOrder
) {
}
