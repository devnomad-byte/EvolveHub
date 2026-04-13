package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建桌面图标请求
 *
 * @param permName    图标名称
 * @param permCode    图标编码（全局唯一，如 app:chat）
 * @param icon       图标名称（lucide-vue-next 组件名）
 * @param gradient    CSS 渐变色
 * @param path       路由路径
 * @param sort       排序号
 * @param defaultWidth  默认窗口宽度
 * @param defaultHeight 默认窗口高度
 * @param minWidth    最小窗口宽度
 * @param minHeight   最小窗口高度
 * @param dockOrder   Dock 栏顺序，-1 不显示
 * @param status     状态（0禁用 1正常）
 */
public record CreateDesktopIconRequest(
        @NotBlank(message = "图标名称不能为空") String permName,
        @NotBlank(message = "图标编码不能为空") String permCode,
        @NotBlank(message = "图标不能为空") String icon,
        @NotBlank(message = "渐变色不能为空") String gradient,
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
