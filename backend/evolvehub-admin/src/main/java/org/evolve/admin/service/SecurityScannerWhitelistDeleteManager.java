package org.evolve.admin.service;

import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.domain.resource.infra.SecurityScannerWhitelistInfra;
import org.evolve.domain.resource.model.SecurityScannerWhitelistEntity;
import org.springframework.stereotype.Service;

/**
 * 删除安全扫描白名单
 */
@Service
public class SecurityScannerWhitelistDeleteManager extends BaseManager<Long, Void> {

    @jakarta.annotation.Resource
    private SecurityScannerWhitelistInfra whitelistInfra;

    @Override
    protected void check(Long id) {
        SecurityScannerWhitelistEntity entity = whitelistInfra.getById(id);
        if (entity == null) {
            throw new BusinessException("白名单记录不存在");
        }
    }

    @Override
    protected Void process(Long id) {
        whitelistInfra.lambdaUpdate()
                .eq(SecurityScannerWhitelistEntity::getId, id)
                .set(SecurityScannerWhitelistEntity::getDeleted, 1)
                .update();
        return null;
    }
}
