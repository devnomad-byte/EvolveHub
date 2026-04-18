package org.evolve.admin.response;

import org.evolve.domain.resource.model.SecurityScannerConfigEntity;

import java.time.LocalDateTime;

/**
 * 安全扫描配置响应
 */
public record SecurityScannerConfigResponse(
        Long id,
        Integer enabled,
        String mode,
        Integer timeout,
        LocalDateTime updateTime
) {
    public static SecurityScannerConfigResponse from(SecurityScannerConfigEntity entity) {
        return new SecurityScannerConfigResponse(
                entity.getId(),
                entity.getEnabled(),
                entity.getMode(),
                entity.getTimeout(),
                entity.getUpdateTime()
        );
    }
}
