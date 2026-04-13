package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.ModelConfigInfra;
import org.evolve.common.model.ModelConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除用户级模型配置业务处理器
 * <p>
 * 校验当前用户必须是资源所有者，且 scope=USER。
 * </p>
 *
 * @author zhao
 */
@Service
public class DeleteUserModelConfigManager extends BaseManager<Long, Void> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Override
    protected void check(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ModelConfigEntity existing = modelConfigInfra.getModelConfigById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "模型配置不存在");
        }
        if (!"USER".equals(existing.getScope()) || !currentUserId.equals(existing.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该模型配置");
        }
    }

    @Override
    protected Void process(Long id) {
        modelConfigInfra.deleteModelConfig(id);
        return null;
    }
}
