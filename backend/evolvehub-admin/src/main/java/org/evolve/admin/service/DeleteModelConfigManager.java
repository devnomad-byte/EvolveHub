package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.infra.ModelConfigInfra;
import org.evolve.common.model.ModelConfigEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除模型配置业务处理器
 * <p>
 * 删除前会校验模型配置是否存在。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/10
 */
@Service
public class DeleteModelConfigManager extends BaseManager<Long, Void> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Override
    protected void check(Long id) {
        ModelConfigEntity existing = modelConfigInfra.getModelConfigById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "模型配置不存在");
        }
    }

    @Override
    protected Void process(Long id) {
        modelConfigInfra.deleteModelConfig(id);
        return null;
    }
}
