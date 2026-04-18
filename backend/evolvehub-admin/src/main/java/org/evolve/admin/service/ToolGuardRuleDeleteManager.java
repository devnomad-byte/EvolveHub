package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.ToolGuardRuleInfra;
import org.evolve.domain.resource.model.ToolGuardRuleEntity;
import org.springframework.stereotype.Service;

/**
 * 删除工具守卫规则
 */
@Service
public class ToolGuardRuleDeleteManager extends BaseManager<Long, Void> {

    @Resource
    private ToolGuardRuleInfra toolGuardRuleInfra;

    @Override
    protected void check(Long id) {
        ToolGuardRuleEntity existing = toolGuardRuleInfra.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "规则不存在: " + id);
        }
        if (existing.getIsBuiltin() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "内置规则不允许删除");
        }
    }

    @Override
    protected Void process(Long id) {
        toolGuardRuleInfra.lambdaUpdate()
                .eq(ToolGuardRuleEntity::getId, id)
                .set(ToolGuardRuleEntity::getDeleted, 1)
                .update();
        return null;
    }
}
