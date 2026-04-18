package org.evolve.admin.service;

import org.evolve.admin.response.SecurityScannerConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SecurityScannerConfigInfra;
import org.springframework.stereotype.Service;

/**
 * 获取安全扫描配置
 */
@Service
public class SecurityScannerConfigGetManager extends BaseManager<Void, SecurityScannerConfigResponse> {

    @jakarta.annotation.Resource
    private SecurityScannerConfigInfra configInfra;

    @Override
    protected void check(Void request) {}

    @Override
    protected SecurityScannerConfigResponse process(Void request) {
        var config = configInfra.getConfig();
        if (config == null) {
            // 返回默认配置
            return new SecurityScannerConfigResponse(1L, 1, "block", 30, null);
        }
        return SecurityScannerConfigResponse.from(config);
    }
}
