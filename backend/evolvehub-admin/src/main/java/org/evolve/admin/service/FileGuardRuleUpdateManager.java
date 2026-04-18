package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateFileGuardRuleRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.FileGuardRuleInfra;
import org.evolve.domain.resource.model.FileGuardRuleEntity;
import org.springframework.stereotype.Service;

/**
 * 更新文件守卫规则
 */
@Service
public class FileGuardRuleUpdateManager extends BaseManager<UpdateFileGuardRuleRequest, Void> {

    @Resource
    private FileGuardRuleInfra fileGuardRuleInfra;

    @Override
    protected void check(UpdateFileGuardRuleRequest request) {
        FileGuardRuleEntity existing = fileGuardRuleInfra.getById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "规则不存在: " + request.id());
        }

        // 内置规则不允许修改关键信息
        if (existing.getIsBuiltin() == 1) {
            if (request.pathPattern() != null || request.pathType() != null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "内置规则不允许修改路径模式");
            }
        }

        // 校验路径类型
        if (request.pathType() != null && !isValidPathType(request.pathType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的路径类型: " + request.pathType());
        }

        // 校验严重级别
        if (request.severity() != null && !isValidSeverity(request.severity())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的严重级别: " + request.severity());
        }
    }

    @Override
    protected Void process(UpdateFileGuardRuleRequest request) {
        FileGuardRuleEntity entity = fileGuardRuleInfra.getById(request.id());

        if (request.name() != null) entity.setName(request.name());
        if (request.pathPattern() != null) entity.setPathPattern(request.pathPattern());
        if (request.pathType() != null) entity.setPathType(request.pathType());
        if (request.tools() != null) entity.setTools(request.tools());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.remediation() != null) entity.setRemediation(request.remediation());
        if (request.severity() != null) entity.setSeverity(request.severity());
        if (request.enabled() != null) entity.setEnabled(request.enabled());

        fileGuardRuleInfra.updateById(entity);
        return null;
    }

    private boolean isValidPathType(String pathType) {
        return "FILE".equals(pathType) || "DIRECTORY".equals(pathType) || "WILDCARD".equals(pathType);
    }

    private boolean isValidSeverity(String severity) {
        return "CRITICAL".equals(severity) || "HIGH".equals(severity)
            || "MEDIUM".equals(severity) || "LOW".equals(severity) || "INFO".equals(severity);
    }
}
