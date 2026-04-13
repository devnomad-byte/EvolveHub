package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新桌面图标请求
 *
 * @param id          图标 ID
 * @param permName    图标名称
 * @param icon        图标名称
 * @param gradient    CSS 渐变色
 * @param path        路由路径
 * @param sort        排序号
 * @param defaultWidth  默认窗口宽度
 * @param defaultHeight 默认窗口高度
 * @param minWidth    最小窗口宽度
 * @param minHeight   最小窗口高度
 * @param dockOrder   Dock 栏顺序
 * @param status      状态
 */
public record UpdateDesktopIconRequest(
        @NotNull(message = "图标ID不能为空") Long id,
        String permName,
        String icon,
        String gradient,
        String path,
        Integer sort,
        Integer defaultWidth,
        Integer defaultHeight,
        Integer minWidth,
        Integer minHeight,
        Integer dockOrder,
        Integer status
) {
}
