package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新模型提供商请求
 *
 * @param id               提供商 ID
 * @param name             提供商名称
 * @param logoUrl          Logo URL
 * @param defaultBaseUrl   默认 Base URL
 * @param sort             排序
 * @param enabled          启用状态（1=启用 0=禁用）
 */
public record UpdateModelProviderRequest(
        @NotNull(message = "提供商 ID 不能为空") Long id,
        String name,
        String logoUrl,
        String defaultBaseUrl,
        Integer sort,
        Integer enabled) {
}
