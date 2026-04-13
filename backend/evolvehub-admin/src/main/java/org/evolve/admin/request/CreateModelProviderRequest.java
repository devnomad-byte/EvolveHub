package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建模型提供商请求
 *
 * @param name             提供商名称
 * @param logoUrl          Logo URL
 * @param defaultBaseUrl   默认 Base URL
 * @param sort             排序
 * @param enabled          启用状态（1=启用 0=禁用）
 */
public record CreateModelProviderRequest(
        @NotBlank(message = "提供商名称不能为空") String name,
        String logoUrl,
        String defaultBaseUrl,
        Integer sort,
        @NotBlank(message = "启用状态不能为空") Integer enabled) {
}
