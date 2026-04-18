package org.evolve.admin.response;

import org.evolve.domain.resource.model.SecurityScannerBlockedHistoryEntity;

import java.time.LocalDateTime;

/**
 * 安全扫描阻断历史响应
 */
public record SecurityScannerBlockedHistoryResponse(
        Long id,
        String skillName,
        String action,
        String contentHash,
        String maxSeverity,
        String findings,
        Long userId,
        String userNickname,
        LocalDateTime createTime
) {
    public static SecurityScannerBlockedHistoryResponse from(SecurityScannerBlockedHistoryEntity entity) {
        return new SecurityScannerBlockedHistoryResponse(
                entity.getId(),
                entity.getSkillName(),
                entity.getAction(),
                entity.getContentHash(),
                entity.getMaxSeverity(),
                entity.getFindings(),
                entity.getUserId(),
                entity.getUserNickname(),
                entity.getCreateTime()
        );
    }
}
