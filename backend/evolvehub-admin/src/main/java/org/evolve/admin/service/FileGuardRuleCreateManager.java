package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateFileGuardRuleRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.FileGuardRuleInfra;
import org.evolve.domain.resource.model.FileGuardRuleEntity;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;

/**
 * 创建文件守卫规则
 */
@Service
public class FileGuardRuleCreateManager extends BaseManager<CreateFileGuardRuleRequest, Long> {

    @Resource
    private FileGuardRuleInfra fileGuardRuleInfra;

    @Override
    protected void check(CreateFileGuardRuleRequest request) {
        // 检查规则ID是否已存在
        FileGuardRuleEntity existing = fileGuardRuleInfra.getByRuleId(request.ruleId());
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "规则ID已存在: " + request.ruleId());
        }

        // 校验路径类型
        if (!isValidPathType(request.pathType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的路径类型: " + request.pathType());
        }

        // 校验严重级别
        if (request.severity() != null && !isValidSeverity(request.severity())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的严重级别: " + request.severity());
        }
    }

    @Override
    protected Long process(CreateFileGuardRuleRequest request) {
        FileGuardRuleEntity entity = new FileGuardRuleEntity();
        entity.setRuleId(request.ruleId());
        entity.setName(request.name());
        entity.setPathPattern(request.pathPattern());
        entity.setPathType(request.pathType());
        entity.setTools(request.tools());
        entity.setDescription(request.description());
        entity.setRemediation(request.remediation());
        entity.setSeverity(request.severity() != null ? request.severity() : "HIGH");
        entity.setIsBuiltin(0); // 自定义规则
        entity.setEnabled(request.enabled() != null ? request.enabled() : 1);
        entity.setCreateBy(StpUtil.getLoginIdAsLong());

        fileGuardRuleInfra.save(entity);
        return entity.getId();
    }

    private boolean isValidPathType(String pathType) {
        return "FILE".equals(pathType) || "DIRECTORY".equals(pathType) || "WILDCARD".equals(pathType);
    }

    private boolean isValidSeverity(String severity) {
        return "CRITICAL".equals(severity) || "HIGH".equals(severity)
            || "MEDIUM".equals(severity) || "LOW".equals(severity) || "INFO".equals(severity);
    }
}
