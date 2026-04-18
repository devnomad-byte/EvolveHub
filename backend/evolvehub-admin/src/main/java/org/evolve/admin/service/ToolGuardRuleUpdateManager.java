package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateToolGuardRuleRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.ToolGuardRuleInfra;
import org.evolve.domain.resource.model.ToolGuardRuleEntity;
import org.springframework.stereotype.Service;

/**
 * 更新工具守卫规则
 */
@Service
public class ToolGuardRuleUpdateManager extends BaseManager<UpdateToolGuardRuleRequest, Void> {

    @Resource
    private ToolGuardRuleInfra toolGuardRuleInfra;

    @Override
    protected void check(UpdateToolGuardRuleRequest request) {
        ToolGuardRuleEntity existing = toolGuardRuleInfra.getById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "规则不存在: " + request.id());
        }

        // 内置规则不允许修改关键信息
        if (existing.getIsBuiltin() == 1) {
            if (request.patterns() != null || request.excludePatterns() != null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "内置规则不允许修改正则表达式");
            }
        }

        // 校验严重级别
        if (request.severity() != null && !isValidSeverity(request.severity())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的严重级别: " + request.severity());
        }
    }

    @Override
    protected Void process(UpdateToolGuardRuleRequest request) {
        ToolGuardRuleEntity entity = toolGuardRuleInfra.getById(request.id());

        if (request.name() != null) entity.setName(request.name());
        if (request.tools() != null) entity.setTools(request.tools());
        if (request.params() != null) entity.setParams(request.params());
        if (request.severity() != null) entity.setSeverity(request.severity());
        if (request.patterns() != null) entity.setPatterns(request.patterns());
        if (request.excludePatterns() != null) entity.setExcludePatterns(request.excludePatterns());
        if (request.category() != null) entity.setCategory(request.category());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.remediation() != null) entity.setRemediation(request.remediation());
        if (request.enabled() != null) entity.setEnabled(request.enabled());

        toolGuardRuleInfra.updateById(entity);
        return null;
    }

    private boolean isValidSeverity(String severity) {
        return "CRITICAL".equals(severity) || "HIGH".equals(severity)
            || "MEDIUM".equals(severity) || "LOW".equals(severity) || "INFO".equals(severity);
    }
}
