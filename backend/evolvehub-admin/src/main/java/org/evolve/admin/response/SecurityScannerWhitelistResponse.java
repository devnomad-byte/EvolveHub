package org.evolve.admin.response;

import org.evolve.domain.resource.model.SecurityScannerWhitelistEntity;

import java.time.LocalDateTime;

/**
 * 安全扫描白名单响应
 */
public record SecurityScannerWhitelistResponse(
        Long id,
        String skillName,
        String contentHash,
        Long addedBy,
        LocalDateTime createTime
) {
    public static SecurityScannerWhitelistResponse from(SecurityScannerWhitelistEntity entity) {
        return new SecurityScannerWhitelistResponse(
                entity.getId(),
                entity.getSkillName(),
                entity.getContentHash(),
                entity.getAddedBy(),
                entity.getCreateTime()
        );
    }
}
