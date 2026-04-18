package org.evolve.admin.service;

import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SecurityScannerBlockedHistoryInfra;
import org.springframework.stereotype.Service;

/**
 * 清除安全扫描阻断历史
 */
@Service
public class SecurityScannerBlockedHistoryDeleteManager extends BaseManager<Void, Void> {

    @jakarta.annotation.Resource
    private SecurityScannerBlockedHistoryInfra historyInfra;

    @Override
    protected void check(Void request) {}

    @Override
    protected Void process(Void request) {
        historyInfra.deleteAll();
        return null;
    }
}
