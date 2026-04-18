package org.evolve.admin.response;

/**
 * 文件守卫配置响应
 */
public record FileGuardConfigResponse(
        Integer enabled,
        String updateTime
) {}
