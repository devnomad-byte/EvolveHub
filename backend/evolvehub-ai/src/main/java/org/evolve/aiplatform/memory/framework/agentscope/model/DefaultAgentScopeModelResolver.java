package org.evolve.aiplatform.memory.framework.agentscope.model;

import io.agentscope.core.model.Model;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.config.ChatModelFactory;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Component;

/**
 * 默认 AgentScope 模型解析器
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultAgentScopeModelResolver implements AgentScopeModelResolver {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ChatModelFactory chatModelFactory;

    @Override
    public Model resolve(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            throw new BusinessException(ResultCode.FAIL, "模型名称不能为空");
        }
        ModelConfigEntity modelConfig = findEnabledModelByName(modelName);
        if (modelConfig == null) {
            throw new BusinessException(ResultCode.FAIL, "模型不存在: " + modelName);
        }
        if (modelConfig.getEnabled() == null || modelConfig.getEnabled() != 1) {
            throw new BusinessException(ResultCode.FAIL, "模型未启用: " + modelConfig.getName());
        }
        return chatModelFactory.createModel(modelConfig);
    }

    /**
     * 大小写无关查询模型，兼容依赖模块尚未刷新到新 API 的场景。
     *
     * @param modelName 模型名称
     * @return 模型配置
     * @author TellyJiang
     * @since 2026-04-16
     */
    ModelConfigEntity findEnabledModelByName(String modelName) {
        String normalizedName = modelName == null ? null : modelName.trim();
        if (normalizedName == null || normalizedName.isBlank()) {
            return null;
        }
        ModelConfigEntity directMatch = modelConfigInfra.getByName(normalizedName);
        if (directMatch != null) {
            return directMatch;
        }
        return modelConfigInfra.lambdaQuery()
                .apply("LOWER(name) = LOWER({0})", normalizedName)
                .last("LIMIT 1")
                .one();
    }
}
