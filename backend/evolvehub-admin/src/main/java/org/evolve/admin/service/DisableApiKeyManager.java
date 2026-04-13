package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ApiKeyInfra;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 禁用 API Key 业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Service
public class DisableApiKeyManager extends BaseManager<Long, Void> {

    @Resource
    private ApiKeyInfra apiKeyInfra;

    @Override
    protected void check(Long userId) {
        if (apiKeyInfra.getByUserId(userId) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "该用户尚未生成 API Key");
        }
    }

    @Override
    protected Void process(Long userId) {
        apiKeyInfra.disableByUserId(userId);
        return null;
    }
}
