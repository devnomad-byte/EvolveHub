package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

/**
 * 对话模型可用性校验支持类
 *
 * @author TellyJiang
 * @since 2026-04-18
 */
@Service
public class AvailableModelSupport {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    /**
     * 校验当前登录用户可用的对话模型
     *
     * @param modelConfigId 模型配置 ID
     * @return 可用模型配置
     * @author TellyJiang
     * @since 2026-04-18
     */
    public ModelConfigEntity requireAvailableChatModel(Long modelConfigId) {
        return requireAvailableChatModel(StpUtil.getLoginIdAsLong(), modelConfigId);
    }

    /**
     * 校验指定用户可用的对话模型
     *
     * @param currentUserId 当前用户 ID
     * @param modelConfigId 模型配置 ID
     * @return 可用模型配置
     * @author TellyJiang
     * @since 2026-04-18
     */
    public ModelConfigEntity requireAvailableChatModel(Long currentUserId, Long modelConfigId) {
        ModelConfigEntity model = modelConfigInfra.getModelConfigById(modelConfigId);
        if (model == null) {
            throw new BusinessException("模型配置不存在");
        }
        if (model.getEnabled() == null || model.getEnabled() != 1) {
            throw new BusinessException("模型配置不可用");
        }
        String modelType = model.getModelType();
        if (modelType != null && !"LLM".equalsIgnoreCase(modelType) && !"CHAT".equalsIgnoreCase(modelType)) {
            throw new BusinessException("模型类型不支持对话");
        }
        if ("USER".equalsIgnoreCase(model.getScope())
                && currentUserId != null
                && model.getOwnerId() != null
                && !currentUserId.equals(model.getOwnerId())) {
            throw new BusinessException("模型配置不可用");
        }
        return model;
    }
}
