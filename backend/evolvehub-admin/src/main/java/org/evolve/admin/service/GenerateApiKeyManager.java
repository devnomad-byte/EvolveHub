package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.GenerateApiKeyRequest;
import org.evolve.admin.response.GenerateApiKeyResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ApiKeyInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.resource.model.ApiKeyEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 生成 API Key 业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>目标用户必须存在</li>
 *     <li>同一用户只能有一个 API Key</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Service
public class GenerateApiKeyManager extends BaseManager<GenerateApiKeyRequest, GenerateApiKeyResponse> {

    @Resource
    private ApiKeyInfra apiKeyInfra;

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(GenerateApiKeyRequest request) {
        if (usersInfra.getById(request.userId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
        if (apiKeyInfra.getByUserId(request.userId()) != null) {
            throw new BusinessException(ResultCode.API_KEY_ALREADY_EXIST, "该用户已有 API Key，请使用重置接口");
        }
    }

    @Override
    protected GenerateApiKeyResponse process(GenerateApiKeyRequest request) {
        ApiKeyEntity entity = apiKeyInfra.createForUser(request.userId());
        return new GenerateApiKeyResponse(entity.getId(), entity.getUserId(), entity.getApiKey());
    }
}
