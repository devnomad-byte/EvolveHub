package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.ToolGuardRuleInfra;
import org.evolve.domain.resource.model.ToolGuardRuleEntity;
import org.springframework.stereotype.Service;

/**
 * 启用/禁用工具守卫规则
 */
@Service
public class ToolGuardRuleToggleManager extends BaseManager<Long, Void> {

    @Resource
    private ToolGuardRuleInfra toolGuardRuleInfra;

    @Override
    protected void check(Long id) {
        ToolGuardRuleEntity existing = toolGuardRuleInfra.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "规则不存在: " + id);
        }
    }

    @Override
    protected Void process(Long id) {
        ToolGuardRuleEntity entity = toolGuardRuleInfra.getById(id);
        entity.setEnabled(entity.getEnabled() == 1 ? 0 : 1);
        toolGuardRuleInfra.updateById(entity);
        return null;
    }
}
