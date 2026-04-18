package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.FileGuardRuleResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.FileGuardRuleInfra;
import org.evolve.domain.resource.model.FileGuardRuleEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询文件守卫规则列表
 */
@Service
public class FileGuardRuleListManager extends BaseManager<Void, List<FileGuardRuleResponse>> {

    @Resource
    private FileGuardRuleInfra fileGuardRuleInfra;

    @Override
    protected void check(Void request) {}

    @Override
    protected List<FileGuardRuleResponse> process(Void request) {
        List<FileGuardRuleEntity> rules = fileGuardRuleInfra.lambdaQuery()
                .eq(FileGuardRuleEntity::getDeleted, 0)
                .orderByAsc(FileGuardRuleEntity::getId)
                .list();

        return rules.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private FileGuardRuleResponse toResponse(FileGuardRuleEntity entity) {
        return new FileGuardRuleResponse(
                entity.getId(),
                entity.getRuleId(),
                entity.getName(),
                entity.getPathPattern(),
                entity.getPathType(),
                entity.getTools(),
                entity.getDescription(),
                entity.getRemediation(),
                entity.getSeverity(),
                entity.getIsBuiltin(),
                entity.getEnabled(),
                entity.getCreateBy(),
                entity.getCreateTime() != null ? entity.getCreateTime().toString() : null,
                entity.getUpdateTime() != null ? entity.getUpdateTime().toString() : null
        );
    }
}
