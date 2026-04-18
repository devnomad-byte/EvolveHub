package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.ToolGuardRuleResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ToolGuardRuleInfra;
import org.evolve.domain.resource.model.ToolGuardRuleEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 获取工具守卫规则列表
 */
@Service
public class ToolGuardRuleListManager extends BaseManager<Void, List<ToolGuardRuleResponse>> {

    @Resource
    private ToolGuardRuleInfra toolGuardRuleInfra;

    @Override
    protected void check(Void request) {}

    @Override
    protected List<ToolGuardRuleResponse> process(Void request) {
        List<ToolGuardRuleEntity> rules = toolGuardRuleInfra.listAll();
        return rules.stream().map(ToolGuardRuleResponse::from).toList();
    }
}
