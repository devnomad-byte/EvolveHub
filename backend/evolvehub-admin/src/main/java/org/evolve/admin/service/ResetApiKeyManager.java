package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.GenerateApiKeyResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ApiKeyInfra;
import org.evolve.domain.resource.model.ApiKeyEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 重置 API Key 业务处理器
 * <p>
 * 为指定用户重新生成 API Key，旧 Key 立即失效。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Service
public class ResetApiKeyManager extends BaseManager<Long, GenerateApiKeyResponse> {

    @Resource
    private ApiKeyInfra apiKeyInfra;

    @Override
    protected void check(Long userId) {
        if (apiKeyInfra.getByUserId(userId) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "该用户尚未生成 API Key");
        }
    }

    @Override
    protected GenerateApiKeyResponse process(Long userId) {
        ApiKeyEntity entity = apiKeyInfra.resetForUser(userId);
        return new GenerateApiKeyResponse(entity.getId(), entity.getUserId(), entity.getApiKey());
    }
}
