package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateToolGuardRuleRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.ToolGuardRuleInfra;
import org.evolve.domain.resource.model.ToolGuardRuleEntity;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;

/**
 * 创建工具守卫规则
 */
@Service
public class ToolGuardRuleCreateManager extends BaseManager<CreateToolGuardRuleRequest, Long> {

    @Resource
    private ToolGuardRuleInfra toolGuardRuleInfra;

    @Override
    protected void check(CreateToolGuardRuleRequest request) {
        // 检查规则ID是否已存在
        ToolGuardRuleEntity existing = toolGuardRuleInfra.getByRuleId(request.ruleId());
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "规则ID已存在: " + request.ruleId());
        }

        // 校验严重级别
        if (!isValidSeverity(request.severity())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的严重级别: " + request.severity());
        }
    }

    @Override
    protected Long process(CreateToolGuardRuleRequest request) {
        ToolGuardRuleEntity entity = new ToolGuardRuleEntity();
        entity.setRuleId(request.ruleId());
        entity.setName(request.name());
        entity.setTools(request.tools());
        entity.setParams(request.params());
        entity.setSeverity(request.severity());
        entity.setPatterns(request.patterns());
        entity.setExcludePatterns(request.excludePatterns());
        entity.setCategory(request.category() != null ? request.category() : "command_injection");
        entity.setDescription(request.description());
        entity.setRemediation(request.remediation());
        entity.setIsBuiltin(0); // 自定义规则
        entity.setEnabled(request.enabled() != null ? request.enabled() : 1);
        entity.setCreateBy(StpUtil.getLoginIdAsLong());

        toolGuardRuleInfra.save(entity);
        return entity.getId();
    }

    private boolean isValidSeverity(String severity) {
        return "CRITICAL".equals(severity) || "HIGH".equals(severity)
            || "MEDIUM".equals(severity) || "LOW".equals(severity) || "INFO".equals(severity);
    }
}
