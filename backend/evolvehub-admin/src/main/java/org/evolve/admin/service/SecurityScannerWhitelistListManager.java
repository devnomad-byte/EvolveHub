package org.evolve.admin.service;

import org.evolve.admin.response.SecurityScannerWhitelistResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SecurityScannerWhitelistInfra;
import org.evolve.domain.resource.model.SecurityScannerWhitelistEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 获取安全扫描白名单列表
 */
@Service
public class SecurityScannerWhitelistListManager extends BaseManager<Void, List<SecurityScannerWhitelistResponse>> {

    @jakarta.annotation.Resource
    private SecurityScannerWhitelistInfra whitelistInfra;

    @Override
    protected void check(Void request) {}

    @Override
    protected List<SecurityScannerWhitelistResponse> process(Void request) {
        List<SecurityScannerWhitelistEntity> list = whitelistInfra.listAll();
        return list.stream().map(SecurityScannerWhitelistResponse::from).toList();
    }
}
