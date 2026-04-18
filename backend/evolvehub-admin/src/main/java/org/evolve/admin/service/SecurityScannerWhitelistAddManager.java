package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import org.evolve.admin.request.AddSecurityScannerWhitelistRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.domain.resource.infra.SecurityScannerWhitelistInfra;
import org.evolve.domain.resource.model.SecurityScannerWhitelistEntity;
import org.springframework.stereotype.Service;

/**
 * 添加安全扫描白名单
 */
@Service
public class SecurityScannerWhitelistAddManager extends BaseManager<AddSecurityScannerWhitelistRequest, Long> {

    @jakarta.annotation.Resource
    private SecurityScannerWhitelistInfra whitelistInfra;

    @Override
    protected void check(AddSecurityScannerWhitelistRequest request) {
        // 检查是否已存在相同的 skillName + contentHash
        if (whitelistInfra.existsBySkillNameAndHash(request.skillName(), request.contentHash())) {
            throw new BusinessException("该技能和哈希值已在白名单中");
        }
    }

    @Override
    protected Long process(AddSecurityScannerWhitelistRequest request) {
        SecurityScannerWhitelistEntity entity = new SecurityScannerWhitelistEntity();
        entity.setSkillName(request.skillName());
        entity.setContentHash(request.contentHash());
        entity.setAddedBy(StpUtil.getLoginIdAsLong());
        whitelistInfra.save(entity);
        return entity.getId();
    }
}
