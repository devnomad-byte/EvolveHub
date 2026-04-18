package org.evolve.admin.service;

import org.evolve.admin.request.UpdateSecurityScannerConfigRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SecurityScannerConfigInfra;
import org.evolve.domain.resource.model.SecurityScannerConfigEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 更新安全扫描配置
 */
@Service
public class SecurityScannerConfigUpdateManager extends BaseManager<UpdateSecurityScannerConfigRequest, Void> {

    @jakarta.annotation.Resource
    private SecurityScannerConfigInfra configInfra;

    @Override
    protected void check(UpdateSecurityScannerConfigRequest request) {}

    @Override
    protected Void process(UpdateSecurityScannerConfigRequest request) {
        SecurityScannerConfigEntity config = configInfra.getConfig();
        if (config == null) {
            config = new SecurityScannerConfigEntity();
            config.setEnabled(request.enabled());
            config.setMode(request.mode());
            config.setTimeout(request.timeout());
            config.setUpdateTime(LocalDateTime.now());
            configInfra.save(config);
        } else {
            configInfra.lambdaUpdate()
                    .eq(SecurityScannerConfigEntity::getId, config.getId())
                    .set(SecurityScannerConfigEntity::getEnabled, request.enabled())
                    .set(SecurityScannerConfigEntity::getMode, request.mode())
                    .set(SecurityScannerConfigEntity::getTimeout, request.timeout())
                    .set(SecurityScannerConfigEntity::getUpdateTime, LocalDateTime.now())
                    .update();
        }
        return null;
    }
}
